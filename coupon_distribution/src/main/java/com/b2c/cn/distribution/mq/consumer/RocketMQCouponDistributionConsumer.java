package com.b2c.cn.distribution.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.b2c.cn.distribution.common.constant.RedisStoreConstant;
import com.b2c.cn.distribution.common.constant.RocketMQStoreConstant;
import com.b2c.cn.distribution.common.enums.CouponTaskStatusEnum;
import com.b2c.cn.distribution.common.enums.CouponTemplateSourceEnum;
import com.b2c.cn.distribution.common.enums.CouponTemplateStatusEnum;
import com.b2c.cn.distribution.common.mqsendtemplate.CouponTemplateDistributionEvent;
import com.b2c.cn.distribution.dao.entity.CouponTaskDO;
import com.b2c.cn.distribution.dao.entity.CouponTaskFailDO;
import com.b2c.cn.distribution.dao.entity.CouponTemplateDO;
import com.b2c.cn.distribution.dao.entity.UserCouponReceiveDO;
import com.b2c.cn.distribution.dao.mapper.CouponTaskFailMapper;
import com.b2c.cn.distribution.dao.mapper.CouponTaskMapper;
import com.b2c.cn.distribution.dao.mapper.CouponTemplateMapper;
import com.b2c.cn.distribution.dao.mapper.UserCouponReceiveMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.b2c.cn.distribution.common.constant.RedisStoreConstant.*;

/**
 * @author zrq
 * 2026/3/3 19:17
 */
@RocketMQMessageListener(
        topic = RocketMQStoreConstant.COUPON_TEMPLATE_TOPIC,
        consumerGroup = RocketMQStoreConstant.COUPON_TASK_DISTRIBUTION_CONSUMER_GROUP,
        selectorType = SelectorType.TAG,
        selectorExpression = "coupon_task_distribution"
)
@RequiredArgsConstructor
@Slf4j(topic = "RocketMQCouponDistributionConsumer")
@Component
public class RocketMQCouponDistributionConsumer implements RocketMQListener<CouponTemplateDistributionEvent> {
    private final CouponTaskMapper couponTaskMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final CouponTaskFailMapper couponTaskFailMapper;
    private final UserCouponReceiveMapper userCouponReceiveMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private final static int BATCH_USER_COUPON_SIZE = 5000;
    private final static String BATCH_SAVE_USER_COUPON_LIST_LUA_PATH = "lua/batch_save_user_coupon_list.lua";

    @Override
    public void onMessage(CouponTemplateDistributionEvent message) {
        log.info("优惠券分发任务执行--->{}", message);
        if (!message.getDistributionEndFlag() && message.getBatchUserSetSize() % BATCH_USER_COUPON_SIZE == 0) {
            decrementAndBatchSaveUserRecord(message);
            return;
        }
        //最后一批
        if (message.getDistributionEndFlag().equals(Boolean.TRUE)) {
            String couponUserReceiveKey = String.format(COUPON_USER_RECEIVE_KEY, message.getCouponTaskId());
            Long RemainingNumber = stringRedisTemplate.opsForSet().size(couponUserReceiveKey);
            message.setBatchUserSetSize(RemainingNumber.intValue());
            decrementAndBatchSaveUserRecord(message);

            List<String> batchUserMaps = stringRedisTemplate.opsForSet().pop(couponUserReceiveKey, Integer.MAX_VALUE);
            // 此时待保存入库用户优惠券列表如果还有值，就意味着可能库存不足引起的
            if (CollUtil.isNotEmpty(batchUserMaps)) {
                // 添加到 t_coupon_task_fail 并标记错误原因，方便后续查看未成功发送的原因和记录
                List<CouponTaskFailDO> couponTaskFailDOList = new ArrayList<>(batchUserMaps.size());
                for (String batchUserMapStr : batchUserMaps) {
                    Map<Object, Object> objectMap = MapUtil.builder()
                            .put("rowNum", JSON.parseObject(batchUserMapStr).get("rowNum"))
                            .put("cause", "优惠券模板库存不足")
                            .build();
                    CouponTaskFailDO couponTaskFailDO = CouponTaskFailDO.builder()
                            .batchId(message.getCouponTaskBatchId())
                            .jsonObject(com.alibaba.fastjson.JSON.toJSONString(objectMap))
                            .build();
                    couponTaskFailDOList.add(couponTaskFailDO);
                }
                // 添加到 t_coupon_task_fail 并标记错误原因
                couponTaskFailMapper.insert(couponTaskFailDOList);
            }

            CouponTaskDO couponTaskDO = CouponTaskDO.builder()
                    .status(CouponTaskStatusEnum.SUCCESS.getValue())
                    .completionTime(DateUtil.date())
                    .build();

            couponTaskMapper.update(couponTaskDO, Wrappers.<CouponTaskDO>lambdaQuery().
                    eq(CouponTaskDO::getId, message.getCouponTaskId()));
        }
    }

    private void decrementAndBatchSaveUserRecord(CouponTemplateDistributionEvent message) {
        //扣减数据库库存
        Integer decrementDBStock = batchDecrementDBStock(message);
        if (decrementDBStock < 1) {
            return;
        }
        String couponUserReceiveKey = String.format(COUPON_USER_RECEIVE_KEY, message.getCouponTaskId());
        List<String> userIdRowNumList = stringRedisTemplate.opsForSet().pop(couponUserReceiveKey, decrementDBStock);
        List<UserCouponReceiveDO> userCouponDOList = new ArrayList<>(userIdRowNumList.size());
        Date now = new Date();
        userIdRowNumList.forEach(each -> {
            JSONObject userIdAndRowNumJsonObject = JSON.parseObject(each);
            DateTime validEndTime = DateUtil.offsetHour(now, JSON.parseObject(message.getCouponTemplateConsumeRule()).getInteger("validityPeriod"));
            UserCouponReceiveDO userCouponDO = UserCouponReceiveDO.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .couponTemplateId(message.getCouponTemplateId())
                    .rowNum(userIdAndRowNumJsonObject.getInteger("rowNum"))
                    .userId(userIdAndRowNumJsonObject.getLong("userId"))
                    .receiveTime(now)
                    .receiveCount(1) // 代表第一次领取该优惠券
                    .validStartTime(now)
                    .validEndTime(validEndTime)
                    .source(CouponTemplateSourceEnum.PLATFORM.getValue())
                    .status(CouponTemplateStatusEnum.ACTIVE.getValue())
                    .createTime(now)
                    .updateTime(now)
                    .delFlag(0)
                    .build();
            userCouponDOList.add(userCouponDO);
        });
        //批量保存用户优惠券
        batchSaveUserCouponList(message.getCouponTemplateId(), message.getCouponTaskBatchId(), userCouponDOList);
        //增加redis用户领券记录
        List<String> list = userCouponDOList.stream().map(UserCouponReceiveDO::getUserId)
                .map(String::valueOf)
                .toList();
        String userIdJsonStr = JSON.toJSONString(list);
        List<String> list1 = userCouponDOList.stream().map(each -> each.getCouponTemplateId() + "_" + each.getId())
                .toList();
        String couponIdJsonStr = JSON.toJSONString(list1);
        List<String> keys = List.of(
                COUPON_USER_RECEIVE_LIMIT_KEY,
                RedisStoreConstant.COUPON_USER_RECEIVE_LIST_KEY.replace("%s", ""),
                String.valueOf(message.getCouponTemplateId())
        );

        List<String> args = List.of(userIdJsonStr, couponIdJsonStr, String.valueOf(DateUtil.date().getTime()),
                String.valueOf(Duration.between(
                        LocalDateTime.now(),
                        message.getValidEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                ).getSeconds())
        );

        DefaultRedisScript<Void> buildLuaScript = Singleton.get(BATCH_SAVE_USER_COUPON_LIST_LUA_PATH, () -> {
            DefaultRedisScript<Void> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(BATCH_SAVE_USER_COUPON_LIST_LUA_PATH)));
            redisScript.setResultType(Void.class);
            return redisScript;
        });
        stringRedisTemplate.execute(buildLuaScript, keys, args.toArray());
        int diff = userIdRowNumList.size() - userCouponDOList.size();
        if (diff > 0) {
            String couponTemplateKey = String.format(COUPON_TEMPLATE_KEY, message.getCouponTemplateId());
            stringRedisTemplate.opsForHash().increment(couponTemplateKey, "stock", diff);
            couponTemplateMapper.incrementCouponTemplateStock(message.getShopNumber(), message.getCouponTemplateId(), diff);
        }
    }

    public void batchSaveUserCouponList(Long couponTemplateId, Long couponTaskBatchId, List<UserCouponReceiveDO> userCouponDOList) {
        try {
            userCouponReceiveMapper.insert(userCouponDOList, userCouponDOList.size());
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof BatchExecutorException) {
                List<CouponTaskFailDO> couponTaskFailDOList = new ArrayList<>();
                List<UserCouponReceiveDO> toRemove = new ArrayList<>();
                userCouponDOList.forEach(each -> {
                    UserCouponReceiveDO couponReceiveDO = userCouponReceiveMapper.selectOne(Wrappers.lambdaQuery(UserCouponReceiveDO.class)
                            .eq(UserCouponReceiveDO::getCouponTemplateId, couponTemplateId)
                            .eq(UserCouponReceiveDO::getUserId, each.getUserId()));
                    if (couponReceiveDO != null) {
                        CouponTaskFailDO failDO = CouponTaskFailDO.builder()
                                .batchId(couponTaskBatchId)
                                .jsonObject("该用户已经领取过优惠券")
                                .build();
                        couponTaskFailDOList.add(failDO);
                        toRemove.add(each);
                    }
                });
                couponTaskFailMapper.insert(couponTaskFailDOList, couponTaskFailDOList.size());
                userCouponDOList.removeAll(toRemove);
                return;
            }
            throw e;
        }
    }

    /**
     * 批量减库存
     *
     * @param message 参数对象
     * @return 真实库存扣减数量
     */
    public Integer batchDecrementDBStock(CouponTemplateDistributionEvent message) {
        Integer decrementDBStock = couponTemplateMapper.decrementDBStock(message.getCouponTemplateId(), message.getBatchUserSetSize(), message.getShopNumber());
        if (decrementDBStock < 1) {
            CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne(Wrappers.lambdaQuery(CouponTemplateDO.class)
                    .eq(CouponTemplateDO::getId, message.getCouponTemplateId())
                    .eq(CouponTemplateDO::getShopNumber, message.getShopNumber())
            );
            message.setBatchUserSetSize(couponTemplateDO.getStock());
            return batchDecrementDBStock(message);
        }
        return message.getBatchUserSetSize();
    }
}
