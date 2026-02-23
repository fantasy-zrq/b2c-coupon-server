package com.b2c.cn.user.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.b2c.cn.starter.exception.ClientException;
import com.b2c.cn.user.admin.common.context.UserAdminContext;
import com.b2c.cn.user.admin.common.enums.UserAdminDelFlagEnum;
import com.b2c.cn.user.admin.dao.entity.UserAdminDO;
import com.b2c.cn.user.admin.dao.mapper.UserAdminMapper;
import com.b2c.cn.user.admin.dto.req.UserAdminCreateReqDTO;
import com.b2c.cn.user.admin.dto.req.UserAdminLoginReqDTO;
import com.b2c.cn.user.admin.file.FileService;
import com.b2c.cn.user.admin.service.UserAdminService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zrq
 * 2026/2/21 15:06
 */
@Service
@RequiredArgsConstructor
@Slf4j(topic = "UserAdminServiceImpl")
public class UserAdminServiceImpl extends ServiceImpl<UserAdminMapper, UserAdminDO> implements UserAdminService {

    private final UserAdminMapper userAdminMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final FileService fileService;
    @Value("${rustfs.endpoint}")
    private String endpoint;
    @Value("${rustfs.bucket}")
    private String bucket;

    @Override
    public void create(UserAdminCreateReqDTO requestParam) {
        UserAdminDO adminDO = BeanUtil.toBean(requestParam, UserAdminDO.class);
        String pwdEncode = passwordEncoder.encode(adminDO.getPassword());
        adminDO.setPassword(pwdEncode);
        userAdminMapper.insert(adminDO);
    }

    @Override
    public void login(UserAdminLoginReqDTO requestParam) {
        UserAdminDO adminDO = userAdminMapper.selectOne(Wrappers.lambdaQuery(UserAdminDO.class)
                .eq(UserAdminDO::getName, requestParam.getName())
                .eq(UserAdminDO::getShopNumber, requestParam.getShopNumber())
                .eq(UserAdminDO::getDelFlag, UserAdminDelFlagEnum.UNDELETE.getCode()));
        if (adminDO == null) {
            log.error("用户不存在");
            throw new ClientException("用户不存在");
        }
        boolean isMatch = passwordEncoder.matches(requestParam.getPassword(), adminDO.getPassword());
        if (!isMatch) {
            log.error("密码错误");
            throw new ClientException("密码错误");
        }
        StpUtil.login(adminDO.getId());
    }

    @Override
    public String image(MultipartFile requestParam) throws IOException {

        String ossKey = fileService.upload(requestParam);
        log.info("上传成功");

        userAdminMapper.update(UserAdminDO.builder()
                        .image(ossKey)
                        .build(),
                Wrappers.lambdaUpdate(UserAdminDO.class)
                        .eq(UserAdminDO::getId, StpUtil.getLoginId())
                        .eq(UserAdminDO::getName, UserAdminContext.get().getName())
                        .eq(UserAdminDO::getShopNumber, UserAdminContext.get().getShopNumber())
                        .eq(UserAdminDO::getDelFlag, UserAdminDelFlagEnum.UNDELETE.getCode()));
        return endpoint + "/" + bucket + "/" + ossKey;
    }
}
