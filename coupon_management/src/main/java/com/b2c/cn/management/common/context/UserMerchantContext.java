package com.b2c.cn.management.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Optional;

/**
 * 用户登录信息存储上下文商家
 */
public final class UserMerchantContext {

    /**
     * <a href="https://github.com/alibaba/transmittable-thread-local" />
     */
    private static final ThreadLocal<UserMerchantInfoDTO> USER_THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置用户至上下文
     *
     * @param user 用户详情信息
     */
    public static void setUser(UserMerchantInfoDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取上下文中用户 ID
     *
     * @return 用户 ID
     */
    public static Long getUserId() {
        UserMerchantInfoDTO userMerchantInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userMerchantInfoDTO).map(UserMerchantInfoDTO::getUserId).orElse(null);
    }

    /**
     * 获取上下文中用户名称
     *
     * @return 用户名称
     */
    public static String getUsername() {
        UserMerchantInfoDTO userMerchantInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userMerchantInfoDTO).map(UserMerchantInfoDTO::getUsername).orElse(null);
    }

    /**
     * 获取上下文中用户店铺编号
     *
     * @return 用户店铺编号
     */
    public static Long getShopNumber() {
        UserMerchantInfoDTO userMerchantInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userMerchantInfoDTO).map(UserMerchantInfoDTO::getShopNumber).orElse(null);
    }

    /**
     * 清理用户上下文
     */
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}