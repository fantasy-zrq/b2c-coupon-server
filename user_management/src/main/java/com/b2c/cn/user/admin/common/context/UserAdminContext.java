package com.b2c.cn.user.admin.common.context;

/**
 * @author zrq
 * 2026/2/22 11:59
 */
public class UserAdminContext {
    public static final ThreadLocal<UserAdminInfo> context = new ThreadLocal<>();

    public static void set(UserAdminInfo userAdminInfo) {
        context.set(userAdminInfo);
    }

    public static UserAdminInfo get() {
        return context.get();
    }

    public static void remove() {
        context.remove();
    }
}
