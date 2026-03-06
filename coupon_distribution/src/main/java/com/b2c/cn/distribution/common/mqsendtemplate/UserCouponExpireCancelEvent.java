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
public class UserCouponExpireCancelEvent {

    /**
     * 推送任务id
     */
    private Long couponTemplateId;

    /**
     * 用户优惠券领取id
     */
    private Long userCouponReceiveId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 延迟时间
     */
    private Long delayTime;
}
