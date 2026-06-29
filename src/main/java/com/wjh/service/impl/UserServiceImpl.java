package com.wjh.service.impl;

import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.dto.UserLoginDTO;
import com.wjh.dto.UserRegisterDTO;
import com.wjh.entity.User;
import com.wjh.mapper.UserMapper;
import com.wjh.result.Result;
import com.wjh.service.UserService;
import com.wjh.utils.CodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result sendCode(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        if(!phone.matches(PHONE_REGEX)) {
            log.info("手机号码格式不正确");
            return Result.error(MessageConstant.PHONE_REGEX_ERROR);
        }
        String key = RedisConstant.SMS_CODE_KEY + phone;
        String code = CodeUtil.getCode();
        stringRedisTemplate.opsForValue().set(key, code, 60, TimeUnit.SECONDS);
        log.info(MessageConstant.PHONE_CODE + "验证码: {}", code);
        return Result.success();
    }

    @Override
    public Result register(UserRegisterDTO userRegisterDTO) {
        String phone = userRegisterDTO.getPhone();
        if(!phone.matches(PHONE_REGEX)) {
            log.info("手机号码格式不正确");
            return Result.error(MessageConstant.PHONE_REGEX_ERROR);
        }
        if(!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            return Result.error(MessageConstant.CONFIRM_PASSWORD_ERROR);
        }
        String key = RedisConstant.SMS_CODE_KEY + phone;
        String code = stringRedisTemplate.opsForValue().get(key);
        if(code == null || !code.equals(userRegisterDTO.getCode())) {
            return Result.error(MessageConstant.CODE_ERROR);
        }
        User user = new User();
        user.setPhone(phone);
        if(userMapper.selectByParams(user) != null) {
            return Result.error(MessageConstant.PHONE_EXIST);
        }
        user.setName(UUID.randomUUID().toString());
        user.setPassword(userRegisterDTO.getPassword());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return Result.success("注册成功");
    }
}
