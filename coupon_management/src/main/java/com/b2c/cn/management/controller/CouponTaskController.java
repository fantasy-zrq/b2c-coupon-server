package com.b2c.cn.management.controller;

import com.b2c.cn.management.dto.req.CouponTaskCreateReqDTO;
import com.b2c.cn.management.service.CouponTaskService;
import com.b2c.cn.starter.annotation.RegularCheckChainFilter;
import com.b2c.cn.starter.result.Result;
import com.b2c.cn.starter.web.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zrq
 * 2026/2/24 13:38
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class CouponTaskController {

    private final CouponTaskService couponTaskService;

    @PostMapping("/create")
    @RegularCheckChainFilter(
            mark = "COUPON_TASK_CREATE_MASK",
            requestParam = "#requestParam"
    )
    public Result<?> create(@ModelAttribute CouponTaskCreateReqDTO requestParam) {
        couponTaskService.create(requestParam, requestParam.getFile());
        return Results.success("优惠券任务创建成功");
    }
}
