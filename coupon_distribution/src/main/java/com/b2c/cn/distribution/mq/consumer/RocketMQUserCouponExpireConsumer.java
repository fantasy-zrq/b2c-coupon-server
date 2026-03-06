package com.b2c.cn.distribution.mq.consumer;

import com.b2c.cn.distribution.common.constant.RocketMQStoreConstant;
import com.b2c.cn.distribution.common.mqsendtemplate.UserCouponExpireCancelEvent;
import com.b2c.cn.distribution.dao.entity.UserCouponReceiveDO;
import com.b2c.cn.distribution.dao.mapper.UserCouponReceiveMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.b2c.cn.distribution.common.constant.RedisStoreConstant.COUPON_USER_RECEIVE_LIST_KEY;

/**
 * @author zrq
 * 2026/3/5 21:20
 */
@RocketMQMessageListener(
        topic = RocketMQStoreConstant.COUPON_TEMPLATE_TOPIC,
        consumerGroup = RocketMQStoreConstant.COUPON_USER_COUPON_EXPIRE_GROUP,
        selectorType = SelectorType.TAG,
        selectorExpression = "user_coupon_expire"
)
@RequiredArgsConstructor
@Slf4j(topic = "RocketMQUserCouponExpireConsumer")
@Component
public class RocketMQUserCouponExpireConsumer implements RocketMQListener<UserCouponExpireCancelEvent> {
    private final StringRedisTemplate stringRedisTemplate;
    private final UserCouponReceiveMapper userCouponReceiveMapper;

    @Override
    public void onMessage(UserCouponExpireCancelEvent message) {
        log.info("RocketMQUserCouponExpireConsumer：{}", message);
        String limitKey = String.format(COUPON_USER_RECEIVE_LIST_KEY, message.getUserId());
        String unionKey = message.getCouponTemplateId() + "_" + message.getUserCouponReceiveId();
        Double score = stringRedisTemplate.opsForZSet().score(limitKey, unionKey);
        if (score == null || score == 0) {
            log.info("用户优惠券已过期,已经从zset中删除：{}", message);
            return;
        }
        stringRedisTemplate.opsForZSet().remove(limitKey, unionKey);
        UserCouponReceiveDO receiveDO = UserCouponReceiveDO.builder()
                .status(3)
                .build();
        userCouponReceiveMapper.update(receiveDO, Wrappers.lambdaUpdate(UserCouponReceiveDO.class)
                .eq(UserCouponReceiveDO::getId, message.getUserCouponReceiveId())
                .eq(UserCouponReceiveDO::getUserId, message.getUserId()));
        log.info("用户优惠券已过期,已更新数据库");
    }
}
