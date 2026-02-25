package com.b2c.cn.management.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * @author zrq
 * 2026/2/12 15:28
 */
@Data
public class CouponTaskCreateReqDTO {
    /**
     * 文件
     */
    private MultipartFile file;

    /**
     * 优惠券批次任务名称
     */
    private String taskName;

    /**
     * 优惠券模板id
     */
    private Long couponTemplateId;

    /**
     * 店铺编号,平台管理的shopNumber是0,商家是店铺编号
     */
    private Long shopNumber;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;
}
