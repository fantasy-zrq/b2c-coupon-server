package com.b2c.cn.distribution.common.mqsendtemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zrq
 * 2026/3/3 15:16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTaskExecuteEvent {

    /**
     * 推送任务id
     */
    private Long couponTaskId;
}
