package com.wjh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.interceptor.LoginInterceptor;
import com.wjh.interceptor.RedisReflashInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RedisReflashInterceptor(stringRedisTemplate, objectMapper))
                .addPathPatterns("/**").order(0);

        registry.addInterceptor(new LoginInterceptor(objectMapper))
                .excludePathPatterns("/user/login/password")
                .excludePathPatterns("/user/send-code")
                .excludePathPatterns("/user/login/code")
                .excludePathPatterns("/user/register")
                .order(1);
    }
}
