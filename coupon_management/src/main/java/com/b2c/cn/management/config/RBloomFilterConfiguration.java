package com.b2c.cn.management.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zrq
 * 2026/2/12 16:43
 */
@Configuration
public class RBloomFilterConfiguration {

    @Bean
    public RBloomFilter<String> couponTemplateQueryBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("couponTemplateQueryBloomFilter");
        bloomFilter.tryInit(1000000L, 0.01);
        return bloomFilter;
    }
}
