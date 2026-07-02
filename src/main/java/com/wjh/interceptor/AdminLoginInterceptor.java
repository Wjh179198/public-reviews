package com.wjh.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.context.AdminBaseContext;
import com.wjh.dto.AdminDTO;
import com.wjh.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminLoginInterceptor implements HandlerInterceptor {

    private ObjectMapper objectMapper;

    public AdminLoginInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AdminDTO adminDTO = AdminBaseContext.get();
        if(adminDTO == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(Result.error("请先登录"))
            );
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        AdminBaseContext.remove();
    }
}
