package com.b2c.cn.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zrq
 * 2026/2/12 15:18
 */
@SpringBootApplication
@MapperScan("com.b2c.cn.management.dao.mapper")
public class CouponManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponManagementApplication.class, args);
    }
}
