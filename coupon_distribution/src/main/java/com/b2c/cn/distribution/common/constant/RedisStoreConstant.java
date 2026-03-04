package com.b2c.cn.distribution.common.constant;

/**
 * @author zrq
 * 2026/3/2 19:29
 */
public class RedisStoreConstant {
    public static final String COUPON_TEMPLATE_KEY = "b2c-system-coupon:template:%s";
    public static final String COUPON_TEMPLATE_QUERY_KEY = "b2c-system-coupon:template:query:%s";
    public static final String COUPON_TEMPLATE_QUERY_BREAK_DOWN_KEY = "b2c-system-coupon:template:breakdown:%s";

    /**
     * 优惠券任务进度KEY,格式为：b2c-system-coupon:task:progress:{任务ID}
     */
    public static final String COUPON_TASK_PROGRESS_KEY = "b2c-system-coupon:task:progress:%s";

    /**
     * 用户优惠券领取KEY,格式为：b2c-system-coupon:task:receive:{任务ID}
     */
    public static final String COUPON_USER_RECEIVE_KEY = "b2c-system-coupon:task:receive:%s";

    /**
     * 用户优惠券领取限制KEY,格式为：b2c-system-coupon:task:receive:limit:
     */
    public static final String COUPON_USER_RECEIVE_LIMIT_KEY = "b2c-system-coupon:task:receive:limit:";

    /**
     * 用户优惠券领取列表KEY,格式为：b2c-system-coupon:task:receive:list:{用户ID}
     */
    public static final String COUPON_USER_RECEIVE_LIST_KEY = "b2c-system-coupon:task:receive:list:%s";
}