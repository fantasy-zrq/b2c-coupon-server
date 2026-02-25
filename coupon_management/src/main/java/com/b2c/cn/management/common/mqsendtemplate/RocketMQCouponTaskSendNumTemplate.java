package com.b2c.cn.management.common.mqsendtemplate;

import cn.hutool.core.date.DateUtil;
import com.b2c.cn.management.dao.entity.CouponTaskDO;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.b2c.cn.management.common.constant.RocketMQStoreConstant.COUPON_TEMPLATE_TOPIC;

/**
 * @author zrq
 * 2026/2/24 14:40
 */
@Component
public class RocketMQCouponTaskSendNumTemplate extends RocketMQMessageSendAbstractTemplate<CouponTaskDO> {

    public RocketMQCouponTaskSendNumTemplate(RocketMQTemplate rocketMQTemplate) {
        super(rocketMQTemplate);
    }

    @Override
    protected RocketMQMessageExtension<CouponTaskDO> buildMessage(CouponTaskDO couponTaskDO) {
        return RocketMQMessageExtension.<CouponTaskDO>builder()
                .payload(
                        MessageBuilder.withPayload(couponTaskDO)
                                .setHeader(MessageConst.PROPERTY_REAL_TOPIC, COUPON_TEMPLATE_TOPIC)
                                .setHeader(MessageConst.PROPERTY_TAGS, "coupon_task_send_num_find")
                                .setHeader(MessageConst.PROPERTY_KEYS, couponTaskDO.getId().toString())
                                .build()
                )
                .delayTime(couponTaskDO.getSendTime() == null ? DateUtil.date().getTime() + (30 * 1000L) : couponTaskDO.getSendTime().getTime())
                .build();
    }

    @Override
    public Boolean sendMessage(CouponTaskDO payload) {
        return super.sendMessage(payload);
    }
}
