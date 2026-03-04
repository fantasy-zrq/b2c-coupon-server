package com.b2c.cn.distribution.dao.mapper;


import com.b2c.cn.distribution.dao.entity.CouponTemplateDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zrq
 * 2026/3/2 19:29
 */
public interface CouponTemplateMapper extends BaseMapper<CouponTemplateDO> {

    Integer decrementDBStock(@Param("couponTemplateId") Long couponTemplateId,
                             @Param("batchUserSetSize") Integer batchUserSetSize,
                             @Param("shopNumber") Long shopNumber);

    void incrementCouponTemplateStock(@Param("shopNumber") Long shopNumber,
                                      @Param("couponTemplateId") Long couponTemplateId,
                                      @Param("diff") int diff);
}
