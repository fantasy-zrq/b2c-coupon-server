package com.b2c.cn.distribution.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.b2c.cn.distribution.common.constant.RedisStoreConstant;
import com.b2c.cn.distribution.common.context.UserContext;
import com.b2c.cn.distribution.common.enums.RedisStockDecrementEnum;
import com.b2c.cn.distribution.common.mqsendtemplate.UserCouponExpireCancelEvent;
import com.b2c.cn.distribution.dao.entity.UserCouponReceiveDO;
import com.b2c.cn.distribution.dao.mapper.CouponTemplateMapper;
import com.b2c.cn.distribution.dao.mapper.UserCouponReceiveMapper;
import com.b2c.cn.distribution.dto.req.CouponTemplateQueryReqDTO;
import com.b2c.cn.distribution.dto.req.CouponTemplateRedeemReqDTO;
import com.b2c.cn.distribution.dto.resp.CouponTemplateQueryRespDTO;
import com.b2c.cn.distribution.mq.producer.RocketMQUserCouponExpireTemplate;
import com.b2c.cn.distribution.service.CouponTemplateService;
import com.b2c.cn.distribution.service.UserCouponReceiveService;
import com.b2c.cn.distribution.utils.UserRedeemStockDecrementReturnCombinedUtil;
import com.b2c.cn.starter.exception.ClientException;
import com.b2c.cn.starter.exception.ServiceException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;

import static com.b2c.cn.distribution.common.constant.RedisStoreConstant.COUPON_USER_RECEIVE_LIST_KEY;

/**
 * @author zrq
 * 2026/3/3 11:10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponReceiveServiceImpl extends ServiceImpl<UserCouponReceiveMapper, UserCouponReceiveDO> implements UserCouponReceiveService {

    private final UserCouponReceiveMapper userCouponReceiveMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final CouponTemplateService couponTemplateService;
    private final StringRedisTemplate stringRedisTemplate;
    private final TransactionTemplate transactionTemplate;
    private final RocketMQUserCouponExpireTemplate rocketMQUserCouponExpireTemplate;
    private static final String STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_LUA_PATH = "lua/stock_decrement_and_save_user_receive.lua";

    @Value("${coupon.save.type}")
    private String saveType;

    @Override
    public void redeemUserCoupon(CouponTemplateRedeemReqDTO requestParam) {
        CouponTemplateQueryRespDTO couponTemplateQueryRespDTO = couponTemplateService.find(BeanUtil.toBean(requestParam, CouponTemplateQueryReqDTO.class));
        boolean isInTime = DateUtil.isIn(new Date(), couponTemplateQueryRespDTO.getValidStartTime(), couponTemplateQueryRespDTO.getValidEndTime());
        if (!isInTime) {
            log.error("优惠券不在使用期限内");
            throw new ClientException("优惠券不在使用期限内");
        }

        DefaultRedisScript<Long> buildLuaScript = Singleton.get(STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_LUA_PATH, () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_LUA_PATH)));
            redisScript.setResultType(Long.class);
            return redisScript;
        });

        JSONObject receiveRule = JSON.parseObject(couponTemplateQueryRespDTO.getReceiveRule());
        String limitReceivePerPerson = receiveRule.getString("limitPerPerson");
        // 限制每人领取String结构，有过期时间
        String limitPerPersonKey = RedisStoreConstant.COUPON_USER_RECEIVE_LIMIT_KEY + UserContext.getUserId() + "_" + couponTemplateQueryRespDTO.getId();

        String couponTemplateKey = String.format(RedisStoreConstant.COUPON_TEMPLATE_KEY, couponTemplateQueryRespDTO.getId());

        long expireAtTime = couponTemplateQueryRespDTO.getValidEndTime().getTime();
        Long executed = stringRedisTemplate.execute(buildLuaScript,
                List.of(couponTemplateKey, limitPerPersonKey),
                limitReceivePerPerson, String.valueOf(expireAtTime / 1000)
        );
        long firstField = UserRedeemStockDecrementReturnCombinedUtil.extractFirstField(executed);
        if (RedisStockDecrementEnum.isError(firstField)) {
            throw new ServiceException(RedisStockDecrementEnum.getMessage(firstField));
        }
        long receiveNumber = UserRedeemStockDecrementReturnCombinedUtil.extractSecondField(executed);
        transactionTemplate.executeWithoutResult(status -> {
            try {
                //借用这个批量的方法扣减库存
                couponTemplateMapper.decrementDBStock(couponTemplateQueryRespDTO.getId(), 1, couponTemplateQueryRespDTO.getShopNumber());
                Date now = new Date();
                DateTime validEndTime = DateUtil.offsetHour(now, JSON.parseObject(couponTemplateQueryRespDTO.getConsumeRule()).getInteger("validityPeriod"));
                UserCouponReceiveDO userCouponReceiveDO = UserCouponReceiveDO.builder()
                        .userId(UserContext.getUserId())
                        .couponTemplateId(couponTemplateQueryRespDTO.getId())
                        .receiveTime(new Date())
                        .receiveCount((int) receiveNumber)
                        .validStartTime(now)
                        .validEndTime(validEndTime)
                        .source(requestParam.getSource())
                        .status(couponTemplateQueryRespDTO.getStatus())
                        .build();
                userCouponReceiveMapper.insert(userCouponReceiveDO);

                if (saveType.equals("direct")) {
                    String receiveLimitKey = String.format(COUPON_USER_RECEIVE_LIST_KEY, UserContext.getUserId());
                    String userCouponItemCacheKey = StrUtil.builder()
                            .append(requestParam.getCouponTemplateId())
                            .append("_")
                            .append(userCouponReceiveDO.getId())
                            .toString();
                    stringRedisTemplate.opsForZSet().add(receiveLimitKey, userCouponItemCacheKey, now.getTime());
                    Double score;
                    try {
                        score = stringRedisTemplate.opsForZSet().score(receiveLimitKey, userCouponItemCacheKey);
                        if (score == null) {
                            log.error("redis丢指令了");
                            stringRedisTemplate.opsForZSet().add(receiveLimitKey, userCouponItemCacheKey, now.getTime());
                        }
                    } catch (Exception e) {
                        log.warn("查询Redis用户优惠券记录为空或抛异常，可能Redis宕机或主从复制数据丢失，基础错误信息：{}", e.getCause().getMessage());
                        // 如果直接抛异常大概率 Redis 宕机了，所以应该写个延时队列向 Redis 重试放入值。为了避免代码复杂性，这里直接写新增，大家知道最优解决方案即可
                        stringRedisTemplate.opsForZSet().add(receiveLimitKey, userCouponItemCacheKey, now.getTime());
                    }
                    //优惠券有效期过了，发送延迟消息
                    UserCouponExpireCancelEvent userCouponExpireCancelEvent = UserCouponExpireCancelEvent.builder()
                            .couponTemplateId(couponTemplateQueryRespDTO.getId())
                            .userCouponReceiveId(userCouponReceiveDO.getId())
                            .userId(userCouponReceiveDO.getUserId())
                            .delayTime(validEndTime.getTime())
                            .build();
                    rocketMQUserCouponExpireTemplate.sendMessage(userCouponExpireCancelEvent);
                }
            } catch (Exception ex) {
                status.setRollbackOnly();
                // 优惠券已被领取完业务异常
                if (ex instanceof ServiceException) {
                    throw (ServiceException) ex;
                }
                if (ex instanceof DuplicateKeyException) {
                    log.error("用户重复领取优惠券，用户ID：{}，优惠券模板ID：{}", UserContext.getUserId(), requestParam.getCouponTemplateId());
                    throw new ServiceException("用户重复领取优惠券");
                }
                throw new ServiceException("优惠券领取异常，请稍候再试");
            }
        });
    }
}
