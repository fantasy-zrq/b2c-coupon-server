package com.b2c.cn.management.common.mqsendtemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.Message;

/**
 * @author zrq
 * 2026/2/15 17:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RocketMQMessageExtension<T> {

    private Message<T> payload;
    private Long delayTime;
}
