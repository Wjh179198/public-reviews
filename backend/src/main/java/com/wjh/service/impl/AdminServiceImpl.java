package com.wjh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.constant.UserStatusConstant;
import com.wjh.context.AdminBaseContext;
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
import com.wjh.vo.AdminVO;
import org.springframework.beans.BeanUtils;
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

    @Override
    public Result banUser(Long userId) {
        User user = userMapper.getById(userId);
        if(user == null) {
            return Result.error(MessageConstant.USER_NOT_EXISTS);
        }
        user.setStatus(UserStatusConstant.BAN_USER);
        userMapper.update(user);
        String banKey = RedisConstant.BAN_USER_KEY;
        stringRedisTemplate.opsForSet().add(banKey, userId.toString());
        return Result.success("用户封禁成功");
    }

    @Override
    public Result unbanUser(Long userId) {
        User user = userMapper.getById(userId);
        if (user == null) {
            return Result.error(MessageConstant.USER_NOT_EXISTS);
        }
        if(user.getShopId() != null) {
            user.setStatus(UserStatusConstant.SHOP_USER);
        } else {
            user.setStatus(UserStatusConstant.COMMON_USER);
        }
        userMapper.update(user);
        String banKey = RedisConstant.BAN_USER_KEY;
        stringRedisTemplate.opsForSet().remove(banKey, userId.toString());
        return Result.success("用户解封成功");
    }

    @Override
    public Result<AdminVO> getInfo() {
        AdminDTO adminDTO = AdminBaseContext.get();
        Admin admin = adminMapper.getByName(adminDTO.getName());
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);
        return Result.success(adminVO);
    }

    @Override
    public Result updateAdmin(AdminLoginDTO adminLoginDTO) {
        Admin admin = new Admin();
        admin.setName(adminLoginDTO.getName());
        if (adminLoginDTO.getPassword() != null || !adminLoginDTO.getPassword().isEmpty()) {
            admin.setPassword(DigestUtils.md5DigestAsHex(adminLoginDTO.getPassword().getBytes()));
        }
        adminMapper.update(admin);
        return Result.success("更新成功");
    }
}
