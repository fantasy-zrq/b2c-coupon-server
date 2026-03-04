package com.b2c.cn.distribution.common.mqsendtemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.Message;

/**
 * @author zrq
 * 2026/3/3 14:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RocketMQMessageExtension<T> {

    private Message<T> payload;
    private Long delayTime;
}
