package com.b2c.cn.management.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zrq
 * 2026/2/15 16:56
 */
@Getter
@RequiredArgsConstructor
public enum CouponTemplateStatusEnum {

    ACTIVE(0),
    ENDING(1);

    private final Integer value;
}
