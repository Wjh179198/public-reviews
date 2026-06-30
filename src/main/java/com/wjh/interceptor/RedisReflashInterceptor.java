package com.wjh.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.constant.RedisConstant;
import com.wjh.context.BaseContext;
import com.wjh.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

public class RedisReflashInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;
    private ObjectMapper objectMapper;

    public RedisReflashInterceptor (StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // 去掉 "Bearer " 前缀
        }
        String json = stringRedisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY + token);
        if(json == null || json.isEmpty()) {
            return true;
        }
        stringRedisTemplate.expire(RedisConstant.USER_LOGIN_KEY + token, RedisConstant.USER_LOGIN_EXPIRE, TimeUnit.SECONDS);
        UserDTO userDTO = objectMapper.readValue(json, UserDTO.class);
        BaseContext.setThreadLocal(userDTO);
        return true;
    }


}
