package com.b2c.cn.starter.annotation.aspect;

import cn.hutool.core.util.HashUtil;
import com.alibaba.fastjson2.JSON;
import com.b2c.cn.starter.annotation.NoDuplicateSubmit;
import com.b2c.cn.starter.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author zrq
 * 2026/2/23 14:29
 */
@Aspect
@Component
@RequiredArgsConstructor
public class NoDuplicateSubmitAspect {
    private final RedissonClient redissonClient;

    @Around("@annotation(com.b2c.cn.starter.annotation.NoDuplicateSubmit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        NoDuplicateSubmit duplicateSubmit = getAnnotation(joinPoint);
        Object[] args = joinPoint.getArgs();
        String noduplicateKey = generateNoDuplicateKey(args);
        Object result = null;
        RLock lock = redissonClient.getLock(noduplicateKey);
        if (!lock.tryLock()) {
            throw new ClientException(duplicateSubmit.value());
        }
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            lock.unlock();
        }
    }

    private NoDuplicateSubmit getAnnotation(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        return method.getAnnotation(NoDuplicateSubmit.class);
    }

    private String generateNoDuplicateKey(Object[] args) {
        return "b2c-system-coupon:duplicate:" + HashUtil.hfHash(JSON.toJSONString(args));
    }
}
