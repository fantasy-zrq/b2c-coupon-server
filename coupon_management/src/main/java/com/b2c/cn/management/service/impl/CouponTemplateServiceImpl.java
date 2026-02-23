package com.b2c.cn.management.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.b2c.cn.management.common.constant.RedisStoreConstant;
import com.b2c.cn.management.common.context.UserMerchantContext;
import com.b2c.cn.management.common.enums.CouponTemplateDeleteEnum;
import com.b2c.cn.management.common.enums.CouponTemplateStatusEnum;
import com.b2c.cn.management.common.mqsendtemplate.RocketMQCouponStatusModifyTemplate;
import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import com.b2c.cn.management.dao.mapper.CouponTemplateMapper;
import com.b2c.cn.management.dto.base.BasePageRespDTO;
import com.b2c.cn.management.dto.req.CouponTemplateIncreaseReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplatePageQueryReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplateReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplateTerminateReqDTO;
import com.b2c.cn.management.dto.resp.CouponTemplatePageQueryRespDTO;
import com.b2c.cn.management.service.CouponTemplateService;
import com.b2c.cn.starter.exception.ClientException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.b2c.cn.management.common.constant.CouponTemplateLogRecordConstant.*;

/**
 * @author zrq
 * 2026/2/12 15:24
 */
@Service
@RequiredArgsConstructor
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplateDO> implements CouponTemplateService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RBloomFilter<String> couponTemplateQueryBloomFilter;
    private final RocketMQCouponStatusModifyTemplate rocketMQCouponStatusModifyTemplate;

    @LogRecord(
            success = COUPON_TEMPLATE_CREATE_LOG_CONTENT,
            type = "CouponTemplate",
            bizNo = "{{#bizNo}}",
            extra = "{{#requestParam.toString()}}"
    )
    @Override
    public void create(CouponTemplateReqDTO requestParam) {
        CouponTemplateDO couponTemplateDO = BeanUtil.toBean(requestParam, CouponTemplateDO.class);
        couponTemplateMapper.insert(couponTemplateDO);
        LogRecordContext.putVariable("bizNo", couponTemplateDO.getId());
        LogRecordContext.putVariable("operatorName", UserMerchantContext.getUsername());
        Map<String, Object> beanMap = BeanUtil.beanToMap(couponTemplateDO, false, true);
        Map<String, String> stringMap = beanMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        List<String> key = List.of(String.format(RedisStoreConstant.COUPON_TEMPLATE_KEY, couponTemplateDO.getId()));
        List<String> args = new ArrayList<>(stringMap.size() * 2 + 1);
        stringMap.forEach((k, v) -> {
            args.add(k);
            args.add(v);
        });
        args.add(String.valueOf(couponTemplateDO.getValidEndTime().getTime() / 1000));
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setLocation(new ClassPathResource("lua/coupon_template_create_script.lua"));
        stringRedisTemplate.execute(redisScript, key, args.toArray());
        rocketMQCouponStatusModifyTemplate.sendMessage(couponTemplateDO);
        couponTemplateQueryBloomFilter.add(couponTemplateDO.getId().toString());
    }

    @Override
    public CouponTemplatePageQueryRespDTO selectPage(CouponTemplatePageQueryReqDTO requestParam) {
        Page<CouponTemplateDO> page = new Page<>();
        page.setCurrent(requestParam.getBasePageReqDTO().getPageNum())
                .setSize(requestParam.getBasePageReqDTO().getPageSize());
        Page<CouponTemplateDO> pageRes = couponTemplateMapper.selectPage(page, Wrappers.lambdaQuery(CouponTemplateDO.class)
                .eq(StrUtil.isNotBlank(requestParam.getName()), CouponTemplateDO::getName, requestParam.getName())
                .eq(CouponTemplateDO::getShopNumber, requestParam.getShopNumber())
                .eq(Objects.nonNull(requestParam.getSource()), CouponTemplateDO::getSource, requestParam.getSource())
                .eq(Objects.nonNull(requestParam.getTarget()), CouponTemplateDO::getTarget, requestParam.getTarget())
                .eq(Objects.equals(requestParam.getTarget(), 0), CouponTemplateDO::getGoods, requestParam.getGoods())
                .eq(Objects.nonNull(requestParam.getType()), CouponTemplateDO::getType, requestParam.getType())
                .ge(Objects.nonNull(requestParam.getValidStartTime()), CouponTemplateDO::getValidStartTime, requestParam.getValidStartTime())
                .le(Objects.nonNull(requestParam.getValidEndTime()), CouponTemplateDO::getValidEndTime, requestParam.getValidEndTime()));
        return CouponTemplatePageQueryRespDTO.builder()
                .basePageRespDTO(BasePageRespDTO.builder()
                        .pageNo(pageRes.getCurrent())
                        .pageSize(pageRes.getSize())
                        .total(pageRes.getTotal())
                        .build())
                .records(pageRes.getRecords())
                .build();
    }

    @LogRecord(
            success = COUPON_TEMPLATE_INCREASE_NUMBER_LOG_CONTENT,
            type = "CouponTemplate",
            bizNo = "{{#bizNo}}",
            extra = "{{#requestParam.toString()}}"
    )
    @Override
    @Transactional(rollbackFor = ClientException.class)
    public void increaseNumber(CouponTemplateIncreaseReqDTO requestParam) {
        CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne(Wrappers.lambdaQuery(CouponTemplateDO.class)
                .eq(CouponTemplateDO::getId, requestParam.getId())
                .eq(CouponTemplateDO::getShopNumber, requestParam.getShopNumber())
                .eq(CouponTemplateDO::getStatus, CouponTemplateStatusEnum.ACTIVE.getValue())
                .eq(CouponTemplateDO::getDelFlag, CouponTemplateDeleteEnum.UNDELETED.getValue()));
        if (couponTemplateDO == null) {
            throw new ClientException("不存在id为：" + requestParam.getId() + "的优惠券");
        }
        LogRecordContext.putVariable("originalData", JSON.toJSONString(couponTemplateDO));
        Integer couponTemplateStock = couponTemplateMapper.incrementStock(requestParam);
        if (couponTemplateStock != 1) {
            throw new ClientException("优惠券id为：" + requestParam.getId() + "的库存增加失败");
        }
        String redisKey = String.format(RedisStoreConstant.COUPON_TEMPLATE_KEY, couponTemplateDO.getId());
        stringRedisTemplate.opsForHash().increment(redisKey, "stock", requestParam.getIncreaseNum());
        LogRecordContext.putVariable("bizNo", couponTemplateDO.getId());
        LogRecordContext.putVariable("operatorName", UserMerchantContext.getUsername());
    }

    @LogRecord(
            success = COUPON_TEMPLATE_TERMINATE_LOG_CONTENT,
            type = "CouponTemplate",
            bizNo = "{{#bizNo}}",
            extra = "{{#requestParam.toString()}}"
    )
    @Override
    @Transactional(rollbackFor = ClientException.class)
    public void termination(CouponTemplateTerminateReqDTO requestParam) {
        CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne(Wrappers.lambdaQuery(CouponTemplateDO.class)
                .eq(CouponTemplateDO::getId, requestParam.getId())
                .eq(CouponTemplateDO::getShopNumber, requestParam.getShopNumber())
                .eq(CouponTemplateDO::getStatus, CouponTemplateStatusEnum.ACTIVE.getValue())
                .eq(CouponTemplateDO::getDelFlag, CouponTemplateDeleteEnum.UNDELETED.getValue()));
        if (couponTemplateDO == null) {
            throw new ClientException("不存在id为：" + requestParam.getId() + "的优惠券");
        }
        LogRecordContext.putVariable("originalData", JSON.toJSONString(couponTemplateDO));
        couponTemplateDO.setStatus(CouponTemplateStatusEnum.ENDING.getValue());
        couponTemplateMapper.update(couponTemplateDO, Wrappers.lambdaUpdate(CouponTemplateDO.class)
                .eq(CouponTemplateDO::getId, requestParam.getId())
                .eq(CouponTemplateDO::getShopNumber, requestParam.getShopNumber()));
        String redisKey = String.format(RedisStoreConstant.COUPON_TEMPLATE_KEY, couponTemplateDO.getId());
        stringRedisTemplate.opsForHash().put(redisKey, "status", String.valueOf(CouponTemplateStatusEnum.ENDING.getValue()));
        LogRecordContext.putVariable("bizNo", couponTemplateDO.getId());
        LogRecordContext.putVariable("operatorName", UserMerchantContext.getUsername());
    }
}
