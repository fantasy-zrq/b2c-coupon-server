package com.b2c.cn.user.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zrq
 * 2026/2/21 17:05
 */
@RequiredArgsConstructor
@Getter
public enum UserAdminSuperFlagEnum {

    SUPER_ADMINISTERED(0, "超级管理员"),
    COMMON_ADMINISTERED(1, "普通管理员");
    private final Integer code;
    private final String value;
}
