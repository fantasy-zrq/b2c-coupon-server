package com.b2c.cn.distribution.dto.req;

import lombok.Data;

/**
 * @author zrq
 * 2026/3/5 19:33
 */
@Data
public class CouponTemplateRedeemReqDTO {
    /**
     * 券来源 0：店铺领取 1：平台发放
     */
    private Integer source;

    /**
     * 店铺编号
     */
    private String shopNumber;

    /**
     * 优惠券模板id
     */
    private String couponTemplateId;
}
