package com.b2c.cn.distribution.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.b2c.cn.distribution.common.context.UserContext;
import com.b2c.cn.distribution.dao.entity.CouponTemplateDO;
import com.b2c.cn.distribution.dao.mapper.CouponTemplateMapper;
import com.b2c.cn.distribution.dto.req.CouponTemplateQueryReqDTO;
import com.b2c.cn.distribution.dto.resp.CouponTemplateQueryRespDTO;
import com.b2c.cn.distribution.service.CouponTemplateService;
import com.b2c.cn.starter.exception.ClientException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.b2c.cn.distribution.common.constant.RedisStoreConstant.COUPON_TEMPLATE_KEY;
import static com.b2c.cn.distribution.common.constant.RedisStoreConstant.COUPON_TEMPLATE_QUERY_KEY;
import static com.b2c.cn.distribution.common.enums.CouponTemplateDeleteEnum.UNDELETED;


/**
 * @author zrq
 * 2026/3/2 19:29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplateDO> implements CouponTemplateService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final RBloomFilter<String> couponTemplateQueryBloomFilter;

    @Override
    public CouponTemplateQueryRespDTO find(CouponTemplateQueryReqDTO requestParam) {
        String couponTemplateRedisKey = String.format(COUPON_TEMPLATE_KEY, requestParam.getCouponTemplateId());
        if (!couponTemplateQueryBloomFilter.contains(String.valueOf(requestParam.getCouponTemplateId()))) {
            log.error("不存在id为-->{}<--的优惠券", requestParam.getCouponTemplateId());
            throw new ClientException("不存在id为-->" + requestParam.getCouponTemplateId() + "<--的优惠券");
        }
        Map<Object, Object> objectMap;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(couponTemplateRedisKey))) {
            objectMap = stringRedisTemplate.opsForHash().entries(couponTemplateRedisKey);
            return BeanUtil.toBean(objectMap, CouponTemplateQueryRespDTO.class);
        }
        //获取10段分段锁
        String lockKey = String.format(COUPON_TEMPLATE_QUERY_KEY,
                requestParam.getCouponTemplateId() + UserContext.getUserId() % 10);
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            objectMap = stringRedisTemplate.opsForHash().entries(couponTemplateRedisKey);
            CouponTemplateQueryRespDTO bean = BeanUtil.toBean(objectMap, CouponTemplateQueryRespDTO.class);
            if (bean != null) {
                return bean;
            }
            CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne(Wrappers.lambdaQuery(CouponTemplateDO.class)
                    .eq(CouponTemplateDO::getId, requestParam.getCouponTemplateId())
                    .eq(CouponTemplateDO::getShopNumber, requestParam.getShopNumber())
                    .eq(CouponTemplateDO::getDelFlag, UNDELETED.getValue()));
            if (couponTemplateDO == null) {
                log.error("不存在id为-->{}<--的优惠券", requestParam.getCouponTemplateId());
                throw new ClientException("不存在id为-->" + requestParam.getCouponTemplateId() + "<--的优惠券");
            }
            String luaScript = """
                    redis.call('HMSET', KEYS[1], unpack(ARGV, 1, #ARGV - 1))
                    redis.call('EXPIREAT', KEYS[1], ARGV[#ARGV])
                    """;
            Map<String, Object> couponTemplateMap = BeanUtil.beanToMap(couponTemplateDO, false, true);
            List<String> key = List.of(couponTemplateRedisKey);
            List<String> args = new ArrayList<>(couponTemplateMap.size() * 2 + 1);
            couponTemplateMap.forEach((k, v) -> {
                args.add(k);
                args.add(v.toString());
            });
            long expireTime = DateUtil.parse(couponTemplateMap.get("validEndTime").toString()).getTime();
            args.add(String.valueOf(expireTime));
            stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), key, args.toArray());
            return BeanUtil.toBean(couponTemplateDO, CouponTemplateQueryRespDTO.class);
        } finally {
            lock.unlock();
        }
    }
}
