package com.b2c.cn.management.mq.consumer;

import com.alibaba.fastjson2.JSON;
import com.b2c.cn.management.common.constant.RocketMQStoreConstant;
import com.b2c.cn.management.dao.entity.CouponTaskDO;
import com.b2c.cn.management.dao.mapper.CouponTaskMapper;
import com.b2c.cn.management.service.CouponTaskService;
import com.b2c.cn.starter.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author zrq
 * 2026/2/15 16:41
 */
@RocketMQMessageListener(
        topic = RocketMQStoreConstant.COUPON_TEMPLATE_TOPIC,
        consumerGroup = RocketMQStoreConstant.COUPON_TASK_CHANGE_CONSUMER_GROUP,
        selectorType = SelectorType.TAG,
        selectorExpression = "coupon_task_send_num_find"
)
@RequiredArgsConstructor
@Slf4j(topic = "RocketMQCouponTaskChangeConsumer")
@Component
public class RocketMQCouponTaskChangeConsumer implements RocketMQListener<MessageExt> {
    private final CouponTaskMapper couponTaskMapper;
    private final CouponTaskService couponTaskService;
    private final FileService fileService;

    @Override
    public void onMessage(MessageExt msgExt) {
        log.info("RocketMQCouponTaskChangeConsumer开始消费消息---->：{}", msgExt);
        CouponTaskDO message = JSON.parseObject(msgExt.getBody(), CouponTaskDO.class);
        CouponTaskDO couponTaskDO = couponTaskMapper.selectById(message.getId());
        if (couponTaskDO.getSendNum() == 0) {
            log.warn("优惠券分发任务重试--{}", couponTaskDO);
            couponTaskService.generateCouponTaskSendNum(couponTaskDO.getId(), fileService.download(couponTaskDO.getFileOss()));
        }
        log.info("RocketMQCouponTaskChangeConsumer消费消息成功---->：{}", message);
    }
}
