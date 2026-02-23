package com.b2c.cn.user.admin.dto.req;

import lombok.Data;

/**
 * @author zrq
 * 2026/2/21 15:12
 */
@Data
public class UserAdminLoginReqDTO {
    /**
     * 管理者昵称
     */
    private String name;

    /**
     * 管理者密码
     */
    private String password;

    /**
     * 店铺编号
     */
    private Long shopNumber;
}
