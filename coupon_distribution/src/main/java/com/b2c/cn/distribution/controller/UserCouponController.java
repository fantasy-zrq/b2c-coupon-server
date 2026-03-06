package com.b2c.cn.distribution.controller;

import com.b2c.cn.distribution.dto.req.CouponTemplateRedeemReqDTO;
import com.b2c.cn.distribution.service.UserCouponReceiveService;
import com.b2c.cn.starter.result.Result;
import com.b2c.cn.starter.web.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zrq
 * 2026/3/5 19:31
 */
@RestController
@RequestMapping("/user/coupon")
@RequiredArgsConstructor
public class UserCouponController {

    private final UserCouponReceiveService userCouponReceiveService;

    @PostMapping("/redeem")
    public Result<Void> redeemUserCoupon(@RequestBody CouponTemplateRedeemReqDTO requestParam) {
        userCouponReceiveService.redeemUserCoupon(requestParam);
        return Results.success();
    }
}
