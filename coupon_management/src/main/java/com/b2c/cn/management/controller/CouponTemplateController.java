package com.b2c.cn.management.controller;

import com.b2c.cn.management.dto.req.CouponTemplateIncreaseReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplatePageQueryReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplateReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplateTerminateReqDTO;
import com.b2c.cn.management.dto.resp.CouponTemplatePageQueryRespDTO;
import com.b2c.cn.management.service.CouponTemplateService;
import com.b2c.cn.starter.annotation.NoDuplicateSubmit;
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
            requestParam = "#requestParam"
    )
    @NoDuplicateSubmit
    public Result<?> create(@RequestBody CouponTemplateReqDTO requestParam) {
        couponTemplateService.create(requestParam);
        return Results.success("优惠券创建成功");
    }

    @PostMapping("/increase-number")
    public Result<?> increaseNumber(@RequestBody CouponTemplateIncreaseReqDTO requestParam) {
        couponTemplateService.increaseNumber(requestParam);
        return Results.success("优惠券数量增加成功");
    }

    @PostMapping("/termination")
    public Result<?> termination(@RequestBody CouponTemplateTerminateReqDTO requestParam) {
        couponTemplateService.termination(requestParam);
        return Results.success("优惠券终止成功");
    }

    @PostMapping("/page")
    public Result<CouponTemplatePageQueryRespDTO> page(@RequestBody CouponTemplatePageQueryReqDTO requestParam) {
        return Results.success(couponTemplateService.selectPage(requestParam));
    }
}
