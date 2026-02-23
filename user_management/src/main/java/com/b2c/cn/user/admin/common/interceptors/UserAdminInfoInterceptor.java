package com.b2c.cn.user.admin.common.interceptors;

import cn.dev33.satoken.stp.StpUtil;
import com.b2c.cn.user.admin.common.context.UserAdminContext;
import com.b2c.cn.user.admin.common.context.UserAdminInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author zrq
 * 2026/2/22 12:03
 */
@Slf4j
public class UserAdminInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (StpUtil.isLogin()) {
            UserAdminInfo adminInfo = UserAdminInfo.builder()
                    .id(StpUtil.getLoginIdAsLong())
                    .name("mark")
                    .phoneNumber(1919616450L)
                    .administratorLevel(1)
                    .shopNumber(1606111L)
                    .build();
            UserAdminContext.set(adminInfo);
            return true;
        } else {
            log.error("用户未登录,上下文信息缺失");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserAdminContext.remove();
    }
}
