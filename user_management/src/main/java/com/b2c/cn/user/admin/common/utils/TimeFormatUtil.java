package com.b2c.cn.user.admin.common.utils;

import cn.hutool.core.date.DateUtil;

/**
 * @author zrq
 * 2026/2/22 10:22
 */
public class TimeFormatUtil {
    public static String format(Long time) {
        return DateUtil.format(DateUtil.date(time), "yyyy-MM-dd-HH:mm:ss");
    }
}
