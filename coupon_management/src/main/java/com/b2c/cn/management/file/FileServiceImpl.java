package com.b2c.cn.management.file;

import cn.hutool.core.date.DateUtil;
import com.b2c.cn.management.common.constant.CouponTaskRustFsConstant;
import com.b2c.cn.starter.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author zrq
 * 2026/2/22 10:06
 */
@Service
@Slf4j(topic = "FileServiceImpl")
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final S3Client s3Client;

    @Value("${rustfs.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String rustFsKey = String.format(
                CouponTaskRustFsConstant.RUSTFSFILEKEY,
                UUID.randomUUID(),
                DateUtil.format(DateUtil.date(DateUtil.current()), "yyyy-MM-dd-HH:mm:ss"), filename
        );

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(rustFsKey)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(request,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return rustFsKey;
    }

    @Override
    public InputStream download(String key) {
        return s3Client.getObject(builder -> builder.bucket(bucket).key(key));
    }
}
