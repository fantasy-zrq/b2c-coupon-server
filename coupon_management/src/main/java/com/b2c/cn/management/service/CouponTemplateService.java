package com.b2c.cn.management.service;

import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import com.b2c.cn.management.dto.req.CouponTemplateIncreaseReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplatePageQueryReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplateReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplateTerminateReqDTO;
import com.b2c.cn.management.dto.resp.CouponTemplatePageQueryRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author zrq
 * 2026/2/12 15:23
 */
public interface CouponTemplateService extends IService<CouponTemplateDO> {
    void create(CouponTemplateReqDTO requestParam);

    CouponTemplatePageQueryRespDTO selectPage(CouponTemplatePageQueryReqDTO requestParam);

    void increaseNumber(CouponTemplateIncreaseReqDTO requestParam);

    void termination(CouponTemplateTerminateReqDTO requestParam);
}
