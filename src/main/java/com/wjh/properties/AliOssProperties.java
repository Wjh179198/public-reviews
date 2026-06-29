package com.wjh.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "reviews.aliyun")
public class AliOssProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String endpoint;
}
