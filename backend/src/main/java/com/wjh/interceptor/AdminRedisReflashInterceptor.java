package com.wjh.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.constant.RedisConstant;
import com.wjh.context.AdminBaseContext;
import com.wjh.dto.AdminDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

public class AdminRedisReflashInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;
    private ObjectMapper objectMapper;

    public AdminRedisReflashInterceptor (ObjectMapper objectMapper, StringRedisTemplate stringRedisTemplate) {
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // 去掉 "Bearer " 前缀
        }
        if(token == null) {
            return true;
        }
        String key = RedisConstant.ADMIN_LOGIN_KEY + token;
        String json = stringRedisTemplate.opsForValue().get(key);
        if(json == null || json.isEmpty()) {
            return true;
        }
        stringRedisTemplate.expire(key, RedisConstant.ADMIN_LOGIN_EXPIRE, TimeUnit.SECONDS);
        AdminDTO adminDTO = objectMapper.readValue(json, AdminDTO.class);
        AdminBaseContext.set(adminDTO);
        return true;
    }
}
