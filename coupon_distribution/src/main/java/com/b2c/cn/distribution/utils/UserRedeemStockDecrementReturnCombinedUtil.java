package com.b2c.cn.distribution.utils;

/**
 * @author zrq
 * 2026/3/5 20:33
 */
public class UserRedeemStockDecrementReturnCombinedUtil {
    /**
     * 2^14 > 9999, 所以用 14 位来表示第二个字段
     */
    private static final int SECOND_FIELD_BITS = 14;

    /**
     * 从组合的 int 中提取第一个字段（0、1或2）
     */
    public static long extractFirstField(long combined) {
        return (combined >> SECOND_FIELD_BITS) & 0b11; // 0b11 即二进制的 11，用于限制结果为 2 位
    }

    /**
     * 从组合的 int 中提取第二个字段（0 到 9999 之间的数字）
     */
    public static long extractSecondField(long combined) {
        return combined & ((1 << SECOND_FIELD_BITS) - 1);
    }

//    public static void main(String[] args) {
//        int i = (2 << 14) + 65;
//        System.out.println("i = " + i);
//        long firstField = extractFirstField(i);
//        System.out.println("firstField = " + firstField);
//        long secondField = extractSecondField(i);
//        System.out.println("secondField = " + secondField);
//    }
}
