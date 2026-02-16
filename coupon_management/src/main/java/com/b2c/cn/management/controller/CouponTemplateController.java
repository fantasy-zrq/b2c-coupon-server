package com.b2c.cn.management.controller;

import com.b2c.cn.management.dto.req.CouponTemplateReqDTO;
import com.b2c.cn.management.service.CouponTemplateService;
import com.b2c.cn.starter.annotation.RegularCheckChainFilter;
import com.b2c.cn.starter.result.Result;
import com.b2c.cn.starter.web.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zrq
 * 2026/2/12 15:22
 */
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponTemplateController {

    private final CouponTemplateService couponTemplateService;

    @PostMapping("/create")
    @RegularCheckChainFilter(
            mark = "COUPON_TEMPLATE_CREATE_MASK",
            requestParam = "#a0"
    )
    public Result<?> create(@RequestBody CouponTemplateReqDTO requestParam) {
        couponTemplateService.create(requestParam);
        return Results.success("优惠券创建成功");
    }
}
