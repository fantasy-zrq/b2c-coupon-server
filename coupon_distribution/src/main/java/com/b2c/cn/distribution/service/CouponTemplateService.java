package com.b2c.cn.distribution.service;

import com.b2c.cn.distribution.dao.entity.CouponTemplateDO;
import com.b2c.cn.distribution.dto.req.CouponTemplateQueryReqDTO;
import com.b2c.cn.distribution.dto.resp.CouponTemplateQueryRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author zrq
 * 2026/3/2 19:29
 */
public interface CouponTemplateService extends IService<CouponTemplateDO> {

    CouponTemplateQueryRespDTO find(CouponTemplateQueryReqDTO requestParam);
}
