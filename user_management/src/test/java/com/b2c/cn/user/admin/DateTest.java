package com.b2c.cn.user.admin;

import cn.hutool.core.date.DateTime;
import com.b2c.cn.user.admin.common.utils.TimeFormatUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author zrq
 * 2026/2/22 10:16
 */
public class DateTest {
    @Test
    public void test(){
        System.out.println();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
        String format = now.format(formatter);
        System.out.println("format = " + format);
    }

    @Test
    public void test2(){
        String format = TimeFormatUtil.format(DateTime.now().getTime());
        System.out.println("format = " + format);
    }

    @Test
    public void test3(){
        int maxValue = Integer.MAX_VALUE;
        System.out.println("maxValue = " + maxValue);
    }
}
