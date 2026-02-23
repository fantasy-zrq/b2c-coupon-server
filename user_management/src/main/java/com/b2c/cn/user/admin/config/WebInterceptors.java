package com.b2c.cn.user.admin.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.b2c.cn.user.admin.common.interceptors.UserAdminInfoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zrq
 * 2026/2/21 18:52
 */
@Configuration
public class WebInterceptors implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/login", "/admin/create");
        registry.addInterceptor(new UserAdminInfoInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/login", "/admin/create");
    }
}
