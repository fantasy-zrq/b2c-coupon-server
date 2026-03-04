package com.b2c.cn.distribution.service.impl;

import com.b2c.cn.distribution.dao.entity.UserCouponReceiveDO;
import com.b2c.cn.distribution.dao.mapper.UserCouponReceiveMapper;
import com.b2c.cn.distribution.service.UserCouponReceiveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author zrq
 * 2026/3/3 11:10
 */
@Service
@RequiredArgsConstructor
public class UserCouponReceiveServiceImpl extends ServiceImpl<UserCouponReceiveMapper, UserCouponReceiveDO> implements UserCouponReceiveService {
}
