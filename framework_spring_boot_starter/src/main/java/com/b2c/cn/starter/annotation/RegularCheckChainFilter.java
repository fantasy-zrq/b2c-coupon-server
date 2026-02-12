package com.b2c.cn.starter.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegularCheckChainFilter {

    String mark();

    String requestParam();
}
