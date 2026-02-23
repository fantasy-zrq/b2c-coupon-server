package com.b2c.cn.user.admin.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zrq
 * 2026/2/22 12:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAdminInfo {
    /**
     * 管理者Id
     */
    private Long id;

    /**
     * 管理者昵称
     */
    private String name;

    /**
     * 管理者手机号
     */
    private Long phoneNumber;

    /**
     * 管理员等级，0:平台管理员，1店铺管理员
     */
    private Integer administratorLevel;

    /**
     * 店铺编号
     */
    private Long shopNumber;
}
