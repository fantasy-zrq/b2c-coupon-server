package com.b2c.cn.user.admin.dto.req;

import lombok.Data;

/**
 * @author zrq
 * 2026/2/21 15:12
 */
@Data
public class UserAdminCreateReqDTO {
    /**
     * 管理者昵称
     */
    private String name;

    /**
     * 管理者密码
     */
    private String password;

    /**
     * 管理者手机号
     */
    private Long phoneNumber;

    /**
     * 管理者个性签名
     */
    private String description;

    /**
     * 管理员等级，0:平台管理员，1店铺管理员
     */
    private Integer administratorLevel;

    /**
     * 店铺编号
     */
    private Long shopNumber;

    /**
     * 管理者头像
     */
    private String image;
}
