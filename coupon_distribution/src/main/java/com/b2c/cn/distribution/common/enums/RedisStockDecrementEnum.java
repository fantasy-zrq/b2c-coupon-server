package com.b2c.cn.distribution.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zrq
 * 2026/3/5 20:33
 */
@RequiredArgsConstructor
@Getter
public enum RedisStockDecrementEnum {
    /**
     * 成功
     */
    SUCCESS(0, "成功"),

    /**
     * 库存不足
     */
    STOCK_INSUFFICIENT(1, "优惠券已被领取完啦"),

    /**
     * 用户已经达到领取上限
     */
    LIMIT_REACHED(2, "用户已经达到领取上限");


    private final long code;

    private final String message;

    public static boolean isError(Long code) {
        for (RedisStockDecrementEnum value : values()) {
            if (value.code == code) {
                return code != SUCCESS.code;
            }
        }
        return false;
    }

    public static String getMessage(Long code) {
        for (RedisStockDecrementEnum value : values()) {
            if (value.code == code) {
                return value.message;
            }
        }
        return "规定以外的code值";
    }
}
