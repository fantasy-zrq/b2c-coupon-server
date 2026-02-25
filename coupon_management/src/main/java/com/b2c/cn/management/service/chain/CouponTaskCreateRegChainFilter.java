package com.b2c.cn.management.service.chain;

import com.b2c.cn.management.common.enums.CouponTaskCreateMarkEnum;
import com.b2c.cn.management.common.enums.CouponTemplateDeleteEnum;
import com.b2c.cn.management.common.enums.CouponTemplateStatusEnum;
import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import com.b2c.cn.management.dao.mapper.CouponTemplateMapper;
import com.b2c.cn.management.dto.req.CouponTaskCreateReqDTO;
import com.b2c.cn.starter.chain.ChainFilterAbstractDefine;
import com.b2c.cn.starter.exception.ClientException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zrq
 * 2026/2/24 14:03
 */
@Component
@Slf4j(topic = "CouponTaskCreateRegChainFilter")
@RequiredArgsConstructor
public class CouponTaskCreateRegChainFilter implements ChainFilterAbstractDefine<CouponTaskCreateReqDTO> {

    private final CouponTemplateMapper couponTemplateMapper;

    @Override
    public void handler(CouponTaskCreateReqDTO requestParam) {
        log.info("任务创建参数校验");
        if (requestParam.getSendType() == 0 && requestParam.getSendTime() != null) {
            throw new ClientException("任务立即执行，不能有发送时间");
        } else if (requestParam.getSendType() == 1 && requestParam.getSendTime() == null) {
            throw new ClientException("任务定时执行，必须设置发送时间");
        } else if (requestParam.getSendType() != 0 && requestParam.getSendType() != 1) {
            throw new ClientException("任务发送类型不合规");
        } else {
            CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne(Wrappers.lambdaQuery(CouponTemplateDO.class)
                    .eq(CouponTemplateDO::getId, requestParam.getCouponTemplateId())
                    .eq(CouponTemplateDO::getShopNumber, requestParam.getShopNumber())
                    .eq(CouponTemplateDO::getDelFlag, CouponTemplateDeleteEnum.UNDELETED.getValue())
                    .eq(CouponTemplateDO::getStatus, CouponTemplateStatusEnum.ACTIVE.getValue())
                    .le(requestParam.getSendTime() != null, CouponTemplateDO::getValidStartTime, requestParam.getSendTime()));
            if (couponTemplateDO == null) {
                throw new ClientException("优惠券模板不存在或者发送时间不正确");
            }
            log.info("CouponTaskCreateRegChainFilter--do--success");
        }
    }

    @Override
    public String mark() {
        return CouponTaskCreateMarkEnum.COUPON_TASK_CREATE_MASK.name();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
