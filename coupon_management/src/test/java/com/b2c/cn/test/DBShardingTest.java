package com.b2c.cn.test;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zrq
 * 2026/2/12 15:11
 */
@SpringBootTest
public class DBShardingTest {

    public CouponTemplateDO buildCouponTemplateDO(BigDecimal termsOfUse, BigDecimal maximumDiscountAmount, Integer target, Integer type, String goods) {
        JSONObject receiveRule = new JSONObject();
        receiveRule.put("limitPerPerson", 1); // 每人限领
        receiveRule.put("usageInstructions", "3"); // 使用说明
        JSONObject consumeRule = new JSONObject();
        consumeRule.put("termsOfUse", termsOfUse); // 使用条件 满 x 元可用
        consumeRule.put("maximumDiscountAmount", maximumDiscountAmount); // 最大优惠金额
        consumeRule.put("explanationOfUnmetConditions", "3"); // 不满足使用条件说明
        if (type == 2) {
            consumeRule.put("discountRate", "0.6"); // 折扣券专属，折扣率
        }
        consumeRule.put("validityPeriod", 48); // 自领取优惠券后有效时间，单位小时
        CouponTemplateDO couponTemplateDO = CouponTemplateDO.builder()
                .name("商品立减券") // 优惠券名称
                .target(target) // 优惠对象 0：商品专属 1：全店通用
                .type(type) // 优惠类型 0：立减券 1：满减券 2：折扣券
                .goods(goods) // 优惠商品编码
                .validStartTime(new Date()) // 有效期开始时间
                .validEndTime(DateUtil.offsetMonth(new Date(), 6)) // 有效期结束时间
                .stock(10) // 库存
                .receiveRule(receiveRule.toString()) // 领取规则
                .consumeRule(consumeRule.toString()) // 消耗规则
                .createTime(new Date())
                .updateTime(new Date())
                .delFlag(0)
                .status(0) // 优惠券状态 0：生效中 1：已结束
                .build();
        return couponTemplateDO;
    }

}
