package com.b2c.cn.distribution.common.mqsendtemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 优惠券模板分发事件
 *
 * @author zrq
 * 2026/3/4 16:05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTemplateDistributionEvent {

    /**
     * 优惠券分发任务id
     */
    private Long couponTaskId;

    /**
     * 优惠券分发任务批量id
     */
    private Long couponTaskBatchId;

    /**
     * 店铺编号
     */
    private Long shopNumber;

    /**
     * 优惠券模板id
     */
    private Long couponTemplateId;

    /**
     * 有效期结束时间
     */
    private Date validEndTime;

    /**
     * 消耗规则
     */
    private String couponTemplateConsumeRule;

    /**
     * 批量保存用户优惠券 Set 长度，默认满 500 才会批量保存数据库
     */
    private Integer batchUserSetSize;

    /**
     * 分发结束标识
     */
    private Boolean distributionEndFlag;
}
