package com.b2c.cn.distribution.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zrq
 * 2026/3/3 14:17
 */
@Getter
@RequiredArgsConstructor
public enum CouponTaskStatusEnum {

    /**
     * 待执行
     */
    PENDING(0),

    /**
     * 执行中
     */
    IN_PROGRESS(1),

    /**
     * 执行失败
     */
    FAILED(2),

    /**
     * 执行成功
     */
    SUCCESS(3),

    /**
     * 取消
     */
    CANCEL(4);


    private final Integer value;
}
