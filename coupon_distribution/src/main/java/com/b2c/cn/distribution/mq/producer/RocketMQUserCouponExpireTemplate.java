package com.b2c.cn.distribution.mq.producer;

import com.b2c.cn.distribution.common.mqsendtemplate.RocketMQMessageExtension;
import com.b2c.cn.distribution.common.mqsendtemplate.RocketMQMessageSendAbstractTemplate;
import com.b2c.cn.distribution.common.mqsendtemplate.UserCouponExpireCancelEvent;
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
public class RocketMQUserCouponExpireTemplate extends RocketMQMessageSendAbstractTemplate<UserCouponExpireCancelEvent> {

    public RocketMQUserCouponExpireTemplate(RocketMQTemplate rocketMQTemplate) {
        super(rocketMQTemplate);
    }

    @Override
    protected RocketMQMessageExtension<UserCouponExpireCancelEvent> buildMessage(UserCouponExpireCancelEvent couponTemplateDistributionEvent) {
        return RocketMQMessageExtension.<UserCouponExpireCancelEvent>builder()
                .payload(MessageBuilder.withPayload(couponTemplateDistributionEvent)
                        .setHeader(MessageConst.PROPERTY_REAL_TOPIC, COUPON_TEMPLATE_TOPIC)
                        .setHeader(MessageConst.PROPERTY_TAGS, "user_coupon_expire")
                        .setHeader(MessageConst.PROPERTY_KEYS, couponTemplateDistributionEvent.getUserId().toString())
                        .build())
                .delayTime(couponTemplateDistributionEvent.getDelayTime())
                .build();
    }

    @Override
    public Boolean sendMessage(UserCouponExpireCancelEvent payload) {
        return super.sendMessage(payload);
    }
}
