package com.b2c.cn.management.service.chain;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.b2c.cn.management.common.enums.CouponTemplateCreateMarkEnum;
import com.b2c.cn.management.dto.req.CouponTemplateReqDTO;
import com.b2c.cn.starter.chain.ChainFilterAbstractDefine;
import com.b2c.cn.starter.exception.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author zrq
 * 2026/2/12 17:00
 */
@Slf4j
@Component
public class CouponTemplateCreateRegChainFilter implements ChainFilterAbstractDefine<CouponTemplateReqDTO> {
    @Override
    public void handler(CouponTemplateReqDTO requestParam) {
        if (requestParam.getName().isEmpty()) {
            throw new ClientException("优惠券名称不合规");
        } else if (requestParam.getTarget().equals(0) && requestParam.getGoods().isEmpty()) {
            throw new ClientException("优惠券商品编码不合规");
        } else if (!JSON.isValid(requestParam.getReceiveRule())) {
            throw new ClientException("优惠券领取规则不合规");
        } else if (!JSON.isValid(requestParam.getConsumeRule())) {
            throw new ClientException("优惠券消费规则不合规");
        } else if (requestParam.getStock() <= 0) {
            throw new ClientException("优惠券库存数量不合规");
        } else if (DateUtil.date(requestParam.getValidStartTime()).isBefore(new Date()) || requestParam.getValidStartTime().getTime() >= requestParam.getValidEndTime().getTime()) {
            throw new ClientException("优惠券有效期时间段不合规");
        } else {
            log.info("CouponTemplateCreateRegChainFilter--do--success");
        }
    }

    @Override
    public String mark() {
        return CouponTemplateCreateMarkEnum.COUPON_TEMPLATE_CREATE_MASK.name();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
