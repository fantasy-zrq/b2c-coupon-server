package com.b2c.cn.distribution.dto.req;

import lombok.Data;

/**
 * @author zrq
 * 2026/3/2 19:29
 */
@Data
public class CouponTemplateQueryReqDTO {

    /**
     * 优惠券模板id
     */
    private Long couponTemplateId;

    /**
     * 店铺编号,平台管理的shopNumber是0,商家是店铺编号
     */
    private Long shopNumber;
}
