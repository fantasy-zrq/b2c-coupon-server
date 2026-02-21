package com.b2c.cn.management.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.b2c.cn.management.common.constant.RedisStoreConstant;
import com.b2c.cn.management.common.mqsendtemplate.RocketMQCouponStatusModifyTemplate;
import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import com.b2c.cn.management.dao.mapper.CouponTemplateMapper;
import com.b2c.cn.management.dto.base.BasePageRespDTO;
import com.b2c.cn.management.dto.req.CouponTemplatePageQueryReqDTO;
import com.b2c.cn.management.dto.req.CouponTemplateReqDTO;
import com.b2c.cn.management.dto.resp.CouponTemplatePageQueryRespDTO;
import com.b2c.cn.management.service.CouponTemplateService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Override
    public void create(CouponTemplateReqDTO requestParam) {
        CouponTemplateDO couponTemplateDO = BeanUtil.toBean(requestParam, CouponTemplateDO.class);
        couponTemplateMapper.insert(couponTemplateDO);
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
}
