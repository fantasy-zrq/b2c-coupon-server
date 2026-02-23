package com.b2c.cn.user.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zrq
 * 2026/2/21 17:26
 */
@RequiredArgsConstructor
@Getter
public enum UserAdminDelFlagEnum {

    UNDELETE(0, "未删除"),
    DELETED(1, "已删除");
    private final Integer code;
    private final String value;
}
