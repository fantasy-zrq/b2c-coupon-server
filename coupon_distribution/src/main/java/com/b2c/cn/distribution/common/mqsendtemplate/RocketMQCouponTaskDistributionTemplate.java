package com.b2c.cn.distribution.common.mqsendtemplate;

import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.b2c.cn.distribution.common.constant.RocketMQStoreConstant.COUPON_TEMPLATE_TOPIC;

/**
 * @author zrq
 * 2026/3/3 14:31
 */
@Component
public class RocketMQCouponTaskDistributionTemplate extends RocketMQMessageSendAbstractTemplate<CouponTemplateDistributionEvent>{

    public RocketMQCouponTaskDistributionTemplate(RocketMQTemplate rocketMQTemplate) {
        super(rocketMQTemplate);
    }

    @Override
    protected RocketMQMessageExtension<CouponTemplateDistributionEvent> buildMessage(CouponTemplateDistributionEvent couponTemplateDistributionEvent) {
        return RocketMQMessageExtension.<CouponTemplateDistributionEvent>builder()
                .payload(MessageBuilder.withPayload(couponTemplateDistributionEvent)
                        .setHeader(MessageConst.PROPERTY_REAL_TOPIC, COUPON_TEMPLATE_TOPIC)
                        .setHeader(MessageConst.PROPERTY_TAGS, "coupon_task_distribution")
                        .setHeader(MessageConst.PROPERTY_KEYS, couponTemplateDistributionEvent.getCouponTaskId().toString())
                        .build())
                .build();
    }

    @Override
    public Boolean sendMessage(CouponTemplateDistributionEvent payload) {
        return super.sendMessage(payload);
    }
}
