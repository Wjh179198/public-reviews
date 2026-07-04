package com.wjh.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "reviews.jwt")
public class JwtProperties {

    private String secretKey;
    private long ttl;
    private String tokenName;
}
