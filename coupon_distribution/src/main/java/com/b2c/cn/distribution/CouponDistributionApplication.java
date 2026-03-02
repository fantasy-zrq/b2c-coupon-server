package com.b2c.cn.distribution;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zrq
 * 2026/3/2 20:36
 */
@SpringBootApplication
@MapperScan("com.b2c.cn.distribution.dao.mapper")
public class CouponDistributionApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponDistributionApplication.class, args);
    }
}
