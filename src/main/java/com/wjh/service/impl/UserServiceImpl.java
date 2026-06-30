package com.wjh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.context.BaseContext;
import com.wjh.dto.UserDTO;
import com.wjh.dto.UserLoginDTO;
import com.wjh.dto.UserRegisterDTO;
import com.wjh.dto.UserUpdateDTO;
import com.wjh.entity.User;
import com.wjh.mapper.UserMapper;
import com.wjh.properties.JwtProperties;
import com.wjh.result.Result;
import com.wjh.service.UserService;
import com.wjh.utils.CodeUtil;
import com.wjh.utils.JwtUtil;
import com.wjh.vo.LoginUserVO;
import com.wjh.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Result sendCode(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        if(phone == null || !phone.matches(PHONE_REGEX)) {
            log.info("手机号码格式不正确");
            return Result.error(MessageConstant.PHONE_REGEX_ERROR);
        }
        String key = RedisConstant.SMS_CODE_KEY + phone;
        String code = CodeUtil.getCode();
        stringRedisTemplate.opsForValue().set(key, code, RedisConstant.SMS_CODE_EXPIRE, TimeUnit.SECONDS);
        log.info(MessageConstant.PHONE_CODE + "验证码: {}", code);
        return Result.success();
    }

    @Override
    public Result register(UserRegisterDTO userRegisterDTO) {
        String phone = userRegisterDTO.getPhone();
        if(!phone.matches(PHONE_REGEX)) {
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
        user.setPassword(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        stringRedisTemplate.delete(key);
        return Result.success("注册成功");
    }

    @Override
    public Result loginByPassword(UserLoginDTO userLoginDTO) throws JsonProcessingException {
        String phone = userLoginDTO.getPhone();
        if(!phone.matches(PHONE_REGEX)) {
            return Result.error(MessageConstant.PHONE_REGEX_ERROR);
        }
        User user = new User();
        user.setPhone(phone);
        User user1 = userMapper.selectByParams(user);
        if(user1 == null) {
            return Result.error(MessageConstant.PHONE_NOT_EXIST);
        }
        if(!user1.getPassword().equals(DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes()))) {
            return Result.error(MessageConstant.PASSWORD_ERROR);
        }
        String token = UUID.randomUUID().toString() + user1.getId();
        String key = RedisConstant.USER_LOGIN_KEY + token;
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user1, userDTO);
        stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(userDTO), RedisConstant.USER_LOGIN_EXPIRE, TimeUnit.SECONDS);
        user1.setPassword(null);
        return Result.success(new LoginUserVO(token, user1));
    }

    @Override
    public Result loginByCode(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        if(phone == null) {
            return Result.error(MessageConstant.PHONE_ISNULL);
        }
        if(!phone.matches(PHONE_REGEX)) {
            return Result.error(MessageConstant.PHONE_REGEX_ERROR);
        }
        User user = new User();
        user.setPhone(phone);
        User user1 = userMapper.selectByParams(user);
        if(user1 == null) {
            return Result.error(MessageConstant.PHONE_NOT_EXIST);
        }
        String key = RedisConstant.SMS_CODE_KEY + phone;
        String code = stringRedisTemplate.opsForValue().get(key);
        if(code == null || !code.equals(userLoginDTO.getCode())) {
            return Result.error(MessageConstant.CODE_ERROR);
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user1, userDTO);
        String token = UUID.randomUUID().toString() + userDTO.getId();
        try {
            stringRedisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY + token, objectMapper.writeValueAsString(userDTO), RedisConstant.USER_LOGIN_EXPIRE, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.info("出现异常: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        stringRedisTemplate.delete(key);
        user1.setPassword(null);
        return Result.success(new LoginUserVO(token, user1));
    }

    @Override
    public Result<User> getInfo() {
        UserDTO userDTO = BaseContext.getThreadLocal();
        User user1 = new User();
        user1.setId(userDTO.getId());
        User user = userMapper.selectByParams(user1);
        user.setPassword(null);
        user.setShopId(null);
        return Result.success(user);
    }

    @Override
    public User update(UserUpdateDTO userUpdateDT) {
        User user = new User();
        user.setId(BaseContext.getThreadLocal().getId());
        User user1 = userMapper.selectByParams(user);
        if(user1 == null) {
            throw new RuntimeException(MessageConstant.USER_NOT_EXISTS);
        }
        if(userUpdateDT.getName() != null && !userUpdateDT.getName().isEmpty()) {
            user1.setName(userUpdateDT.getName());
        }
        if(userUpdateDT.getAddress() != null && !userUpdateDT.getAddress().isEmpty()) {
            user1.setAddress(userUpdateDT.getAddress());
        }
        if(userUpdateDT.getImage() != null && !userUpdateDT.getImage().isEmpty()) {
            user1.setImage(userUpdateDT.getImage());
        }
        userMapper.update(user1);
        return user1;
    }

    @Override
    public List<UserVO> searchUser(String keyword) {
        if(keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> userList = userMapper.getListByName(keyword);
        List<UserVO> userVOList = userList.stream().map(user -> UserVO.builder()
                .id(user.getId())
                .name(user.getName())
                .image(user.getImage())
                .address(user.getAddress())
                .fansCounts(user.getFansCounts())
                .followerCounts(user.getFollowerCounts())
                .status(user.getStatus())
                .build()).collect(Collectors.toList());
        return userVOList;
    }

}
