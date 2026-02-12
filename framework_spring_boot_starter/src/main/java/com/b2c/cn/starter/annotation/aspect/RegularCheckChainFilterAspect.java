package com.b2c.cn.starter.annotation.aspect;

import com.b2c.cn.starter.annotation.RegularCheckChainFilter;
import com.b2c.cn.starter.chain.ChainFilterContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author zrq
 * 2026/2/10 14:53
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RegularCheckChainFilterAspect {

    private final ChainFilterContext chainFilterContext;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();
    @Around("@annotation(regularCheckChainFilter)")
    public Object ChainFilterVerify(ProceedingJoinPoint joinPoint, RegularCheckChainFilter regularCheckChainFilter) throws Throwable {
        // 1. 获取注解里的 mark
        String mark = regularCheckChainFilter.mark();

        // 2. 解析 SpEL 拿到真实的 requestParam 对象
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String paramExpression = regularCheckChainFilter.requestParam();

        Object requestParam = null;
        if (StringUtils.hasText(paramExpression)) {
            EvaluationContext context = new MethodBasedEvaluationContext(joinPoint.getTarget(), method, args, discoverer);
            requestParam = parser.parseExpression(paramExpression).getValue(context);
        } else if (args.length > 0) {
            // 如果没写 param，默认取第一个参数
            requestParam = args[0];
        }

        // 3. 执行责任链校验
        // 这里的 requestParam 无论是 User 还是 Order，都通过 Object 传入，靠下游多态或泛型处理
        chainFilterContext.handler(mark, requestParam);

        // 4. 校验通过，继续执行原业务方法
        return joinPoint.proceed();
    }
}
