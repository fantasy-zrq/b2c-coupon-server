package com.b2c.cn.user.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author zrq
 * 2026/2/21 14:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_user_admin")
public class UserAdminDO {
    /**
     * 管理者Id
     */
    private Long id;

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
     * 管理者头像
     */
    private String image;

    /**
     * 管理员等级，0:平台管理员，1店铺管理员
     */
    private Integer administratorLevel;

    /**
     * 店铺编号
     */
    private Long shopNumber;

    /**
     * 删除标记 (0:未删除, 1:已删除)
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
