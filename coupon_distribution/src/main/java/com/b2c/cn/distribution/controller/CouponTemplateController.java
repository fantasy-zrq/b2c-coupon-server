package com.b2c.cn.distribution.controller;


import com.b2c.cn.distribution.dto.req.CouponTemplateQueryReqDTO;
import com.b2c.cn.distribution.dto.resp.CouponTemplateQueryRespDTO;
import com.b2c.cn.distribution.service.CouponTemplateService;
import com.b2c.cn.starter.result.Result;
import com.b2c.cn.starter.web.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author zrq
 * 2026/3/2 19:29
 */
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponTemplateController {

    private final CouponTemplateService couponTemplateService;

    @GetMapping("/find")
    public Result<CouponTemplateQueryRespDTO> find(@RequestBody CouponTemplateQueryReqDTO requestParam) {
        return Results.success(couponTemplateService.find(requestParam));
    }
}
