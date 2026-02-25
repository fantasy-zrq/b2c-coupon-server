package com.b2c.cn.starter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * @author zrq
 * 2026/2/22 10:03
 */
public class S3Config {
    @Value("${rustfs.endpoint}")
    private String endpoint;

    @Value("${rustfs.accessKey}")
    private String accessKey;

    @Value("${rustfs.secretKey}")
    private String secretKey;

    @Value("${rustfs.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .forcePathStyle(true)
                .build();
    }
}
