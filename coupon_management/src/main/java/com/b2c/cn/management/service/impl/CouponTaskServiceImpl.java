package com.b2c.cn.management.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.b2c.cn.management.common.context.UserMerchantContext;
import com.b2c.cn.management.common.enums.CouponTaskStatusEnum;
import com.b2c.cn.management.common.enums.CouponTemplateDeleteEnum;
import com.b2c.cn.management.common.enums.CouponTemplateStatusEnum;
import com.b2c.cn.management.common.mqsendtemplate.RocketMQCouponTaskSendNumTemplate;
import com.b2c.cn.management.dao.entity.CouponTaskDO;
import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import com.b2c.cn.management.dao.mapper.CouponTaskMapper;
import com.b2c.cn.management.dao.mapper.CouponTemplateMapper;
import com.b2c.cn.management.dto.req.CouponTaskCreateReqDTO;
import com.b2c.cn.management.service.CouponTaskService;
import com.b2c.cn.management.service.excel.RowCountListener;
import com.b2c.cn.starter.exception.ClientException;
import com.b2c.cn.starter.file.FileService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zrq
 * 2026/2/24 13:37
 */
@Service
@Slf4j(topic = "CouponTaskServiceImpl")
@RequiredArgsConstructor
public class CouponTaskServiceImpl extends ServiceImpl<CouponTaskMapper, CouponTaskDO> implements CouponTaskService {

    private final CouponTaskMapper couponTaskMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RocketMQCouponTaskSendNumTemplate rocketMQCouponTaskSendNumTemplate;
    private final FileService fileService;

    private final ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() << 1,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    @Override
    public void create(CouponTaskCreateReqDTO requestParam, MultipartFile file) {
        CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne(Wrappers.lambdaQuery(CouponTemplateDO.class)
                .eq(CouponTemplateDO::getId, requestParam.getCouponTemplateId())
                .eq(CouponTemplateDO::getShopNumber, requestParam.getShopNumber())
                .eq(CouponTemplateDO::getDelFlag, CouponTemplateDeleteEnum.UNDELETED.getValue())
                .eq(CouponTemplateDO::getStatus, CouponTemplateStatusEnum.ACTIVE.getValue()));
        if (couponTemplateDO == null) {
            throw new RuntimeException("优惠券模板不存在,无法下发任务。");
        }
        //将inputStream保存到OSS中，方便后续判断任务情况
        String ossKey;
        try {
            ossKey = fileService.upload(file);
        } catch (IOException e) {
            log.error("文件上传OSS失败。", e);
            throw new ClientException("文件上传OSS失败");
        }
        CouponTaskDO couponTaskDO = CouponTaskDO.builder()
                .shopNumber(requestParam.getShopNumber())
                .couponTemplateId(requestParam.getCouponTemplateId())
                .operatorId(UserMerchantContext.getUserId())
                .taskName(requestParam.getTaskName())
                .fileName(file.getOriginalFilename())
                .fileOss(ossKey)
                .sendNum(0)//等到优惠券发放数量后更新
                .status(CouponTaskStatusEnum.IN_PROGRESS.getValue())
                .notifyType(requestParam.getNotifyType())
                .sendType(requestParam.getSendType())
                .sendTime(requestParam.getSendTime())
                .build();
        couponTaskMapper.insert(couponTaskDO);
        try {
            InputStream inputStream = file.getInputStream();
            if (couponTaskDO.getSendType() == 0) {
                executorService.execute(() -> generateCouponTaskSendNum(couponTaskDO.getId(), inputStream));
            }
        } catch (IOException e) {
            log.error("获取文件输入流失败。", e);
            throw new ClientException("获取文件输入流失败。");
        }
        rocketMQCouponTaskSendNumTemplate.sendMessage(couponTaskDO);
    }

    @Override
    public void generateCouponTaskSendNum(Long couponTaskId, InputStream inputStream) {
        RowCountListener listener = new RowCountListener();
        EasyExcel.read(inputStream, listener).sheet().doRead();
        couponTaskMapper.update(
                CouponTaskDO.builder()
                        .sendNum(listener.getRowCount())
                        .status(CouponTaskStatusEnum.SUCCESS.getValue())
                        .completionTime(DateUtil.date())
                        .build(),
                Wrappers.lambdaQuery(CouponTaskDO.class)
                        .eq(CouponTaskDO::getId, couponTaskId)
        );
    }
}
