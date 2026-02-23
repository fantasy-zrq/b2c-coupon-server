package com.b2c.cn.user.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zrq
 * 2026/2/21 14:40
 */
@MapperScan("com.b2c.cn.user.admin.dao.mapper")
@SpringBootApplication
public class AdminManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminManagementApplication.class, args);
    }
}
