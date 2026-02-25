package com.b2c.cn.starter.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zrq
 * 2026/2/22 10:05
 */
public interface FileService {
    String upload(MultipartFile file) throws IOException;

    InputStream download(String key);
}
