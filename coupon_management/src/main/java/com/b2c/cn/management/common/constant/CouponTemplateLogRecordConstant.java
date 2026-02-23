package com.b2c.cn.management.common.constant;

/**
 * @author zrq
 * 2026/2/21 11:32
 */
public class CouponTemplateLogRecordConstant {
    public static final String COUPON_TEMPLATE_CREATE_LOG_CONTENT = "{CURRENT_USER{''}} 用户创建优惠券：{{#requestParam.name}}，" +
            "优惠对象：{COMMON_ENUM_PARSE{'CouponTemplateTargetEnum' + '_' + #requestParam.target}}，" +
            "优惠类型：{COMMON_ENUM_PARSE{'CouponTemplateTypeEnum' + '_' + #requestParam.type}}，" +
            "库存数量：{{#requestParam.stock}}，" +
            "优惠商品编码：{{#requestParam.goods}}，" +
            "有效期开始时间：{{#requestParam.validStartTime}}，" +
            "有效期结束时间：{{#requestParam.validEndTime}}，" +
            "领取规则：{{#requestParam.receiveRule}}，" +
            "消耗规则：{{#requestParam.consumeRule}};";
}
