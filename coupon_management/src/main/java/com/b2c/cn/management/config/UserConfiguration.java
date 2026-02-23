package com.b2c.cn.management.config;

import com.b2c.cn.management.common.context.UserMerchantContext;
import com.b2c.cn.management.common.context.UserMerchantInfoDTO;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 用户相关配置类
 */
@Configuration
public class UserConfiguration implements WebMvcConfigurer {

    /**
     * 用户信息传输拦截器
     */
    @Bean
    public UserTransmitInterceptor userTransmitInterceptor() {
        return new UserTransmitInterceptor();
    }

    /**
     * 添加用户信息传递过滤器至相关路径拦截
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userTransmitInterceptor())
                .addPathPatterns("/**");
    }

    static class UserTransmitInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) throws Exception {
            // 用户属于非核心功能，这里先通过模拟的形式代替。后续如果需要后管展示，会重构该代码
            UserMerchantInfoDTO userMerchantInfoDTO = new UserMerchantInfoDTO("2025754387968270338", "mark", 1606111L);
            UserMerchantContext.setUser(userMerchantInfoDTO);
            return true;
        }

        @Override
        public void afterCompletion(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler, Exception exception) throws Exception {
            UserMerchantContext.removeUser();
        }
    }
}
