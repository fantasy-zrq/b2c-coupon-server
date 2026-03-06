package com.b2c.cn.test;

import com.b2c.cn.management.CouponManagementApplication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author zrq
 * 2026/3/5 16:03
 */
@SpringBootTest(classes = CouponManagementApplication.class)
public class RedisKeySerializeTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testSerialize() {
        redisTemplate.setKeySerializer(new CustomerRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        Person person = new Person("张三", 18);
        redisTemplate.opsForValue().set("person1", person, 5, TimeUnit.MINUTES);
    }

    @RequiredArgsConstructor
    static class CustomerRedisSerializer implements RedisSerializer<String> {
        private static final String PREFIX = "serial:test:";

        @Override
        public byte[] serialize(String key) throws SerializationException {
            return (PREFIX + key).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String deserialize(byte[] bytes) throws SerializationException {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Person {
        private String name;
        private Integer age;
    }
}
