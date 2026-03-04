package com.b2c.cn.distribution.mq.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.b2c.cn.distribution.common.constant.RocketMQStoreConstant;
import com.b2c.cn.distribution.common.enums.CouponTaskStatusEnum;
import com.b2c.cn.distribution.common.enums.CouponTemplateDeleteEnum;
import com.b2c.cn.distribution.common.enums.CouponTemplateStatusEnum;
import com.b2c.cn.distribution.common.mqsendtemplate.RocketMQCouponTaskDistributionTemplate;
import com.b2c.cn.distribution.dao.entity.CouponTaskDO;
import com.b2c.cn.distribution.dao.entity.CouponTemplateDO;
import com.b2c.cn.distribution.dao.mapper.CouponTaskFailMapper;
import com.b2c.cn.distribution.dao.mapper.CouponTaskMapper;
import com.b2c.cn.distribution.dao.mapper.CouponTemplateMapper;
import com.b2c.cn.distribution.excel.CouponTaskExcelObject;
import com.b2c.cn.distribution.excel.ReadExcelDistributionListener;
import com.b2c.cn.starter.exception.ClientException;
import com.b2c.cn.starter.file.FileService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * @author zrq
 * 2026/3/3 14:17
 * 触发优惠券任务，分发优惠券
 */
@RocketMQMessageListener(
        topic = RocketMQStoreConstant.COUPON_TEMPLATE_TOPIC,
        consumerGroup = RocketMQStoreConstant.COUPON_TASK_CHANGE_CONSUMER_GROUP,
        selectorType = SelectorType.TAG,
        selectorExpression = "coupon_task_send_num_find"
)
@RequiredArgsConstructor
@Slf4j(topic = "RocketMQCouponTaskDistributionConsumer")
@Component
public class RocketMQCouponTaskDistributionConsumer implements RocketMQListener<MessageExt> {
    private final CouponTaskMapper couponTaskMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final CouponTaskFailMapper couponTaskFailMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final FileService fileService;
    private final RocketMQCouponTaskDistributionTemplate rocketMQCouponTaskDistributionTemplate;

    @Override
    public void onMessage(MessageExt msgExt) {
        log.info("RocketMQCouponTaskDistributionConsumer正式开始优惠券任务发放---->：{}", msgExt);
        CouponTaskDO message = JSON.parseObject(msgExt.getBody(), CouponTaskDO.class);
        CouponTaskDO taskDO = couponTaskMapper.selectById(message.getId());
        if (ObjectUtil.notEqual(taskDO.getStatus(), CouponTaskStatusEnum.IN_PROGRESS.getValue())) {
            log.error("优惠券推送任务:{} 状态异常--->{}<---", taskDO.getId(), taskDO.getStatus());
            throw new ClientException("优惠券推送任务:" + taskDO.getId() + " 状态异常--->" + taskDO.getStatus() + "<---");
        }
        CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne(Wrappers.lambdaQuery(CouponTemplateDO.class)
                .eq(CouponTemplateDO::getId, taskDO.getCouponTemplateId())
                .eq(CouponTemplateDO::getShopNumber, taskDO.getShopNumber())
                .eq(CouponTemplateDO::getDelFlag, CouponTemplateDeleteEnum.UNDELETED.getValue()));
        if (ObjectUtil.notEqual(couponTemplateDO.getStatus(), CouponTemplateStatusEnum.ACTIVE.getValue())) {
            log.error("优惠券:{} 状态异常--->{}<---", couponTemplateDO.getId(), couponTemplateDO.getStatus());
            throw new ClientException("优惠券:" + couponTemplateDO.getId() + " 状态异常--->" + couponTemplateDO.getStatus() + "<---");

        }
        InputStream inputStream = fileService.download(message.getFileOss());
        ReadExcelDistributionListener listener = new ReadExcelDistributionListener(
                couponTaskFailMapper,
                stringRedisTemplate,
                taskDO,
                couponTemplateDO,
                rocketMQCouponTaskDistributionTemplate
        );
        EasyExcel.read(inputStream, CouponTaskExcelObject.class, listener).sheet().doRead();
    }
}
