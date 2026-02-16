package com.b2c.cn.management.common.mqsendtemplate;

import com.b2c.cn.starter.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;

import java.util.Objects;

/**
 * @author zrq
 * 2026/2/13 16:25
 */
@Slf4j
@RequiredArgsConstructor
public abstract class RocketMQMessageSendAbstractTemplate<T> {

    private final RocketMQTemplate rocketMQTemplate;

    protected RocketMQMessageExtension<T> buildMessage(T t) {
        return null;
    }

    public Boolean sendMessage(T payload) {
        RocketMQMessageExtension<T> msgExt = buildMessage(payload);
        Message<T> message = msgExt.getPayload();
        String topic = (String) message.getHeaders().get(MessageConst.PROPERTY_REAL_TOPIC);
        String tag = (String) message.getHeaders().get(MessageConst.PROPERTY_TAGS);
        String destination = tag == null ? topic : topic + ":" + tag;
        Long delayTime = msgExt.getDelayTime();
        try {
            if (Objects.isNull(delayTime)) {
                rocketMQTemplate.syncSend(destination, message);
                log.info("即时消息发送成功：{}", message);
            } else {
                                //syncSendDeliverTimeMills
                rocketMQTemplate.syncSendDeliverTimeMills(destination, message, msgExt.getDelayTime());
                log.info("定时投递消息发送成功：{}", message);
            }
        } catch (Exception e) {
            log.error("发送消息失败：{}", message);
            throw new ClientException("RocketMQMessageSendTemplate消息发送失败");
        }
        return true;
    }
}
