package com.b2c.cn.management.dto.req;

import lombok.Data;

/**
 * @author zrq
 * 2026/2/12 15:28
 */
@Data
public class CouponTemplateTerminateReqDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 店铺编号
     */
    private Long shopNumber;
}
