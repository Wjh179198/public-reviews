package com.wjh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.interceptor.AdminLoginInterceptor;
import com.wjh.interceptor.AdminRedisReflashInterceptor;
import com.wjh.interceptor.UserLoginInterceptor;
import com.wjh.interceptor.UserRedisReflashInterceptor;
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
        registry.addInterceptor(new UserRedisReflashInterceptor(stringRedisTemplate, objectMapper))
                .addPathPatterns("/user/**")
                .addPathPatterns("/shop/**")
                .addPathPatterns("/blog/**")
                .addPathPatterns("/comment/**")
                .addPathPatterns("/voucher/**")
                .addPathPatterns("/order/**")
                .addPathPatterns("/upload/image")
                .order(0);

        registry.addInterceptor(new UserLoginInterceptor(objectMapper))
                .addPathPatterns("/user/**")
                .addPathPatterns("/shop/**")
                .addPathPatterns("/blog/**")
                .addPathPatterns("/comment/**")
                .addPathPatterns("/voucher/**")
                .addPathPatterns("/order/**")
                .addPathPatterns("/upload/image")
                .excludePathPatterns("/user/login/password")
                .excludePathPatterns("/user/send-code")
                .excludePathPatterns("/user/login/code")
                .excludePathPatterns("/user/register")
                .order(1);

        registry.addInterceptor(new AdminRedisReflashInterceptor(objectMapper, stringRedisTemplate))
                .addPathPatterns("/admin/**")
                .order(3);

        registry.addInterceptor(new AdminLoginInterceptor(objectMapper))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .order(4);
    }
}
