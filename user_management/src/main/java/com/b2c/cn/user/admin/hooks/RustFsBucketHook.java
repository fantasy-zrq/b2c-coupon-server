package com.b2c.cn.user.admin.hooks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/**
 * @author zrq
 * 2026/2/22 11:44
 */
@Component
@Slf4j(topic = "RustFsBucketHook")
@RequiredArgsConstructor
public class RustFsBucketHook implements InitializingBean {
    private final S3Client s3Client;

    @Value("${rustfs.bucket}")
    private String bucketName;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("S3 存储桶 [{}] 检查通过", bucketName);
        } catch (NoSuchBucketException e) {
            log.warn("存储桶 [{}] 不存在，尝试自动创建...", bucketName);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("存储桶 [{}] 创建成功", bucketName);
        } catch (Exception e) {
            log.error("无法连接到 RustFS 存储服务，请检查配置: {}", e.getMessage());
        }
    }
}
