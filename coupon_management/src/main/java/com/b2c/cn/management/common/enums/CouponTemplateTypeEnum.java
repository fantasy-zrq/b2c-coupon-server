package com.b2c.cn.management.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zrq
 * 2026/2/21 11:40
 */
@RequiredArgsConstructor
public enum CouponTemplateTypeEnum {
    /**
     * 立减券
     */
    FIXED_DISCOUNT(0, "立减券"),

    /**
     * 满减券
     */
    THRESHOLD_DISCOUNT(1, "满减券"),

    /**
     * 折扣券
     */
    DISCOUNT_COUPON(2, "折扣券");

    @Getter
    private final int type;

    @Getter
    private final String value;

    /**
     * 根据 type 找到对应的 value
     *
     * @param type 要查找的类型代码
     * @return 对应的描述值，如果没有找到抛异常
     */
    public static String findValueByType(int type) {
        for (CouponTemplateTypeEnum target : CouponTemplateTypeEnum.values()) {
            if (target.getType() == type) {
                return target.getValue();
            }
        }
        throw new IllegalArgumentException();
    }
}
