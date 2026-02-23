package com.b2c.cn.starter.config;

import com.b2c.cn.starter.annotation.aspect.NoDuplicateSubmitAspect;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;

/**
 * @author zrq
 * 2026/2/23 15:15
 */
public class NoDuplicateSubmitConfiguration {

    @Bean
    public NoDuplicateSubmitAspect noDuplicateSubmitAspect(RedissonClient redissonClient) {
        return new NoDuplicateSubmitAspect(redissonClient);
    }
}
