package com.b2c.cn.user.admin.controller;

import com.b2c.cn.starter.annotation.RegularCheckChainFilter;
import com.b2c.cn.starter.exception.ClientException;
import com.b2c.cn.starter.result.Result;
import com.b2c.cn.starter.web.Results;
import com.b2c.cn.user.admin.dto.req.UserAdminCreateReqDTO;
import com.b2c.cn.user.admin.dto.req.UserAdminLoginReqDTO;
import com.b2c.cn.user.admin.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zrq
 * 2026/2/21 15:05
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserAdminService userAdminService;

    @RegularCheckChainFilter(
            mark = "USER_ADMIN_CREATE_MASK",
            requestParam = "#requestParam"
    )
    @PostMapping("/create")
    public Result<?> create(@RequestBody UserAdminCreateReqDTO requestParam) {
        userAdminService.create(requestParam);
        return Results.success("创建成功");
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody UserAdminLoginReqDTO requestParam) {
        userAdminService.login(requestParam);
        return Results.success("登录成功");
    }

    @PostMapping("/image")
    public Result<String> image(@RequestBody MultipartFile requestParam) {
        try {
            return Results.success(userAdminService.image(requestParam));
        } catch (IOException e) {
            throw new ClientException("头像上传失败");
        }
    }
}

