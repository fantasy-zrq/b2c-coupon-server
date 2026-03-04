package com.b2c.cn.distribution.utils;

/**
 * @author zrq
 * 2026/3/4 16:05
 */
public class StockDecrementReturnCombinedUtil {

    /**
     * 2^9 > 500, 所以用 9 位来表示第二个字段
     */
    private static final int SECOND_FIELD_BITS = 15;

    /**
     * 将两个字段组合成一个int
     */
    public static int combineFields(Boolean decrementFlag, Integer userRecord) {
        return (decrementFlag ? 1 : 0) << SECOND_FIELD_BITS | userRecord;
    }

    /**
     * 从组合的int中提取第一个字段（0或1）
     */
    public static boolean extractFirstField(Long combined) {
        return (combined >> SECOND_FIELD_BITS) != 0;
    }

    /**
     * 从组合的int中提取第二个字段（1到5000之间的数字）
     */
    public static int extractSecondField(Integer combined) {
        return combined & ((1 << SECOND_FIELD_BITS) - 1);
    }
}
