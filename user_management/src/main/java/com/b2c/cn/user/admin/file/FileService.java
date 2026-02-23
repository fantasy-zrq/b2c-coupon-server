package com.b2c.cn.user.admin.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zrq
 * 2026/2/22 10:05
 */
public interface FileService {
    String upload(MultipartFile file) throws IOException;
}
