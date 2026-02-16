package com.b2c.cn.management.mq.consumer;

import com.alibaba.fastjson2.JSON;
import com.b2c.cn.management.common.constant.RocketMQStoreConstant;
import com.b2c.cn.management.common.enums.CouponTemplateStatusEnum;
import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import com.b2c.cn.management.dao.mapper.CouponTemplateMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author zrq
 * 2026/2/15 16:41
 */
@RocketMQMessageListener(
        topic = RocketMQStoreConstant.COUPON_TEMPLATE_TOPIC,
        consumerGroup = RocketMQStoreConstant.COUPON_TEMPLATE_STATUS_CHANGE_CONSUMER_GROUP,
        selectorType = SelectorType.TAG,
        selectorExpression = "coupon_status_modify"
)
@RequiredArgsConstructor
@Slf4j(topic = "RocketMQCouponStatusChangeConsumer")
@Component
public class RocketMQCouponStatusChangeConsumer implements RocketMQListener<MessageExt> {
    private final CouponTemplateMapper couponTemplateMapper;

    @Override
    public void onMessage(MessageExt msgExt) {
        log.info("RocketMQCouponStatusChangeConsumer开始消费消息---->：{}", msgExt);
        CouponTemplateDO message = JSON.parseObject(msgExt.getBody(), CouponTemplateDO.class);
        message.setStatus(CouponTemplateStatusEnum.ENDING.getValue());
        message.setUpdateTime(new Date());
        couponTemplateMapper.update(message, Wrappers.lambdaUpdate(CouponTemplateDO.class)
                .eq(CouponTemplateDO::getShopNumber, message.getShopNumber())
                .eq(CouponTemplateDO::getId, message.getId()));
        log.info("RocketMQCouponStatusChangeConsumer消费消息成功---->：{}", message);
    }
}
