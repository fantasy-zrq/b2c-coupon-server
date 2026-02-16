package com.b2c.cn.management.common.mqsendtemplate;

import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.b2c.cn.management.common.constant.RocketMQStoreConstant.COUPON_TEMPLATE_TOPIC;

/**
 * @author zrq
 * 2026/2/13 17:56
 */
@Component
@Slf4j(topic = "RocketMQCouponStatusModify")
public class RocketMQCouponStatusModifyTemplate extends RocketMQMessageSendAbstractTemplate<CouponTemplateDO> {

    public RocketMQCouponStatusModifyTemplate(RocketMQTemplate rocketMQTemplate) {
        super(rocketMQTemplate);
    }

    @Override
    protected RocketMQMessageExtension<CouponTemplateDO> buildMessage(CouponTemplateDO couponTemplateDO) {
        return RocketMQMessageExtension.<CouponTemplateDO>builder()
                .payload(MessageBuilder.withPayload(couponTemplateDO)
                        .setHeader(MessageConst.PROPERTY_REAL_TOPIC, COUPON_TEMPLATE_TOPIC)
                        .setHeader(MessageConst.PROPERTY_TAGS, "coupon_status_modify")
                        .setHeader(MessageConst.PROPERTY_KEYS, couponTemplateDO.getId().toString())
                        .build())
                //这里的延迟时间是基于毫秒级别的
                .delayTime(couponTemplateDO.getValidEndTime().before(new Date()) ? null : couponTemplateDO.getValidEndTime().getTime())
                .build();
    }

    @Override
    public Boolean sendMessage(CouponTemplateDO payload) {
        return super.sendMessage(payload);
    }
}
