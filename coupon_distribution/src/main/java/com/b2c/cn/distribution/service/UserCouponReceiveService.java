package com.b2c.cn.distribution.service;

import com.b2c.cn.distribution.dao.entity.UserCouponReceiveDO;
import com.b2c.cn.distribution.dto.req.CouponTemplateRedeemReqDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author zrq
 * 2026/3/3 11:09
 */
public interface UserCouponReceiveService extends IService<UserCouponReceiveDO> {
    void redeemUserCoupon(CouponTemplateRedeemReqDTO requestParam);
}
