package com.b2c.cn.user.admin.service;

import com.b2c.cn.user.admin.dao.entity.UserAdminDO;
import com.b2c.cn.user.admin.dto.req.UserAdminCreateReqDTO;
import com.b2c.cn.user.admin.dto.req.UserAdminLoginReqDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zrq
 * 2026/2/21 15:06
 */
public interface UserAdminService extends IService<UserAdminDO> {
    void create(UserAdminCreateReqDTO requestParam);

    void login(UserAdminLoginReqDTO requestParam);

    String image(MultipartFile requestParam) throws IOException;
}
