package com.wjh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.dto.AdminDTO;
import com.wjh.dto.AdminLoginDTO;
import com.wjh.entity.Admin;
import com.wjh.entity.User;
import com.wjh.mapper.AdminMapper;
import com.wjh.mapper.UserMapper;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.AdminService;
import com.wjh.vo.AdminLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result login(AdminLoginDTO adminLoginDTO) {
        Admin admin = adminMapper.getByName(adminLoginDTO.getName());
        if (admin == null) {
            return Result.error(MessageConstant.USER_NOT_EXISTS);
        }
        if (!DigestUtils.md5DigestAsHex(adminLoginDTO.getPassword().getBytes()).equals(admin.getPassword())) {
            return Result.error(MessageConstant.PASSWORD_ERROR);
        }
        String token = UUID.randomUUID().toString();
        String key = RedisConstant.ADMIN_LOGIN_KEY + token;
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setId(admin.getId());
        adminDTO.setName(admin.getName());
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(adminDTO), RedisConstant.ADMIN_LOGIN_EXPIRE, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
        admin.setPassword(null);
        return Result.success(new AdminLoginVO(token, admin));
    }

    @Override
    public Result<PageResult> UsersList(Long id, String name, Integer status, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        User user = new User();
        user.setStatus(status);
        user.setId(id);
        user.setName(name);
        List<User> userList = userMapper.getListByParam(user);
        if (userList == null || userList.isEmpty()) {
            return Result.success(new PageResult(0L, new ArrayList(), page, pageSize));
        }
        for (int i = 0; i < userList.size(); i++) {
            userList.get(i).setPassword(null);
        }
        Page<User> userPage = (Page<User>) userList;
        return Result.success(new PageResult(userPage.getTotal(), userPage.getResult(), userPage.getPages(), userPage.getPageSize()));
    }

    @Override
    public Result<User> getUserById(Long userId) {
        User user = userMapper.getById(userId);
        if (user == null) {
            return Result.error(MessageConstant.USER_NOT_EXISTS);
        }
        user.setPassword(null);
        return Result.success(user);
    }
}
