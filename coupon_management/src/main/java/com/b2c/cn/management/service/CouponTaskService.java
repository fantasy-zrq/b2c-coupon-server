package com.b2c.cn.management.service;

import com.b2c.cn.management.dao.entity.CouponTaskDO;
import com.b2c.cn.management.dto.req.CouponTaskCreateReqDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author zrq
 * 2026/2/24 13:37
 */
public interface CouponTaskService extends IService<CouponTaskDO> {
    void create(CouponTaskCreateReqDTO requestParam, MultipartFile file);

    void generateCouponTaskSendNum(Long couponTaskId, InputStream inputStream);
}
