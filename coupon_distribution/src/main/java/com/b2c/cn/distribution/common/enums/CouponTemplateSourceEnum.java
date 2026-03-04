package com.b2c.cn.distribution.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zrq
 * 2026/3/3 19:17
 */
@RequiredArgsConstructor
public enum CouponTemplateSourceEnum {

    /**
     * 店铺券
     */
    SHOP(0),

    /**
     * 平台券
     */
    PLATFORM(1);

    @Getter
    private final Integer value;
}
