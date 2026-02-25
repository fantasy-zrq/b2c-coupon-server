package com.b2c.cn.management.dao.entity;

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
 * 2026/2/24 13:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_coupon_task")
public class CouponTaskDO {
    /**
     * ID
     */
    private Long id;

    /**
     * 店铺编号
     */
    private Long shopNumber;

    /**
     * 优惠券模板ID
     */
    private Long couponTemplateId;

    /**
     * 操作人
     */
    private Long operatorId;

    /**
     * 批次ID
     */
    private Long batchId;

    /**
     * 优惠券批次任务名称
     */
    private String taskName;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件OSS地址
     */
    private String fileOss;

    /**
     * 发放优惠券数量
     */
    private Integer sendNum;

    /**
     * 通知方式，可组合使用 0：站内信 1：弹框推送 2：邮箱 3：短信
     */
    private String notifyType;

    /**
     * 发送类型 0：立即发送 1：定时发送
     */
    private Integer sendType;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 状态 0：待执行 1：执行中 2：执行失败 3：执行成功 4：取消
     */
    private Integer status;

    /**
     * 完成时间
     */
    private Date completionTime;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}