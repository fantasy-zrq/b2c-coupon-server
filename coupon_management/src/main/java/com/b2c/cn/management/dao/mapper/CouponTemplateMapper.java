package com.b2c.cn.management.dao.mapper;

import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import com.b2c.cn.management.dto.req.CouponTemplateIncreaseReqDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zrq
 * 2026/2/12 15:20
 */
public interface CouponTemplateMapper extends BaseMapper<CouponTemplateDO> {
    Integer incrementStock(@Param("requestParam") CouponTemplateIncreaseReqDTO requestParam);
}
