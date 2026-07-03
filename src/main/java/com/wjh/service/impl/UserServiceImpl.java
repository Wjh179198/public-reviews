package com.wjh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.constant.UserStatusConstant;
import com.wjh.context.BaseContext;
import com.wjh.dto.*;
import com.wjh.entity.User;
import com.wjh.mapper.UserMapper;
import com.wjh.properties.JwtProperties;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.UserService;
import com.wjh.utils.CodeUtil;
import com.wjh.vo.LoginUserVO;
import com.wjh.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
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
        return Result.success("验证码已发送");
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
        stringRedisTemplate.opsForSet().add(RedisConstant.USER_FOLLOW_KEY + user.getId().toString(), "0");
        stringRedisTemplate.opsForSet().add(RedisConstant.USER_FAN_KEY + user.getId().toString(), "0");
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
        if(user1.getStatus().equals(UserStatusConstant.BAN_USER)) {
            return Result.error(MessageConstant.USER_BAN_ERROR);
        }
        String token = UUID.randomUUID().toString() + user1.getId();
        String key = RedisConstant.USER_LOGIN_KEY + token;
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user1, userDTO);
        stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(userDTO), RedisConstant.USER_LOGIN_EXPIRE, TimeUnit.SECONDS);
        user1.setPassword(null);
        return Result.success(MessageConstant.LOGIN_SUCCESS, new LoginUserVO(token, user1));
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
        String key = RedisConstant.SMS_CODE_KEY + phone;
        String code = stringRedisTemplate.opsForValue().get(key);
        if(code == null || !code.equals(userLoginDTO.getCode())) {
            return Result.error(MessageConstant.CODE_ERROR);
        }
        User user = new User();
        user.setPhone(phone);
        User user1 = userMapper.selectByParams(user);
        if(user1 == null) {
            return Result.error(MessageConstant.PHONE_NOT_EXIST);
        }
        if(user1.getStatus().equals(UserStatusConstant.BAN_USER)) {
            return Result.error(MessageConstant.USER_BAN_ERROR);
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
        return Result.success(MessageConstant.LOGIN_SUCCESS, new LoginUserVO(token, user1));
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
        User user1 = userMapper.getById(BaseContext.getThreadLocal().getId());
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
        List<UserVO> userVOList = userList.stream()
                .filter(user -> user.getStatus().equals(UserStatusConstant.BAN_USER))
                .map(user -> UserVO.builder()
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

    @Override
    public UserVO getOtherUser(Long userId) {
        User user1 = userMapper.getById(userId);
        if(user1 == null) {
            throw new RuntimeException(MessageConstant.USER_NOT_EXISTS);
        }
        if(user1.getStatus().equals(UserStatusConstant.BAN_USER)) {
            throw new RuntimeException(MessageConstant.USER_BAN_ERROR);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user1, userVO);
        return userVO;
    }

    @Override
    public Boolean checkIsFollow(Long userId) {
        Boolean res  = stringRedisTemplate.opsForSet().isMember(RedisConstant.USER_FAN_KEY + userId.toString(), BaseContext.getThreadLocal().getId().toString());
        return res;
    }

    @Override
    public PageResult getFansList(Long userId, Integer page, Integer pageSize) {
        String key = RedisConstant.USER_FAN_KEY + userId.toString();
        Set<String> fans = stringRedisTemplate.opsForSet().members(key);
        fans.remove("0");
        if(fans == null || fans.isEmpty()) {
            return PageResult.builder().total(0L).records(new ArrayList<>()).pages(0).pageSize(pageSize).build();
        }
        List<Long> ids = fans.stream().map(Long::parseLong).collect(Collectors.toList());
        List<Long> pageIds = new ArrayList<>();
        for(int i = (page - 1) * pageSize; i < Math.min(ids.size(), page * pageSize); i++) {
            pageIds.add(ids.get(i));
        }
        List<User> users = userMapper.getList(pageIds);
        List<UserVO> userVOList = users.stream().map(user -> UserVO.builder()
                .id(user.getId())
                .name(user.getName())
                .image(user.getImage())
                .address(user.getAddress())
                .fansCounts(user.getFansCounts())
                .followerCounts(user.getFollowerCounts())
                .status(user.getStatus())
                .build()).collect(Collectors.toList());
        return PageResult.builder()
                .total(Long.valueOf(fans.size()))
                .records(userVOList)
                .pages(page)
                .pageSize(pageSize)
                .build();
    }

    @Override
    public PageResult getFollowingsList(Long userId, Integer page, Integer pageSize) {
        String key = RedisConstant.USER_FOLLOW_KEY + userId.toString();
        Set<String> followings = stringRedisTemplate.opsForSet().members(key);
        followings.remove("0");
        if(followings == null || followings.isEmpty()) {
            return PageResult.builder().total(0L).records(new ArrayList<>()).pages(0).pageSize(pageSize).build();
        }
        List<Long> ids = followings.stream().map(Long::parseLong).collect(Collectors.toList());
        List<Long> pageIds = new ArrayList<>();
        for(int i = (page - 1) * pageSize; i < Math.min(ids.size(), page * pageSize); i++) {
            pageIds.add(ids.get(i));
        }
        List<User> users = userMapper.getList(pageIds);
        List<UserVO> userVOList = users.stream().map(user -> UserVO.builder()
                .id(user.getId())
                .name(user.getName())
                .image(user.getImage())
                .address(user.getAddress())
                .fansCounts(user.getFansCounts())
                .followerCounts(user.getFollowerCounts())
                .status(user.getStatus())
                .build()).collect(Collectors.toList());
        return PageResult.builder()
                .total(Long.valueOf(followings.size()))
                .records(userVOList)
                .pages(page)
                .pageSize(pageSize)
                .build();
    }

    @Override
    public List<UserVO> getCommonFollowsList(Long userId) {
        String key1 = RedisConstant.USER_FOLLOW_KEY + userId.toString();
        String key2 = RedisConstant.USER_FOLLOW_KEY + BaseContext.getThreadLocal().getId().toString();
        Set<String> commonFollowers = stringRedisTemplate.opsForSet().intersect(key1, key2);
        if(commonFollowers == null || commonFollowers.isEmpty()) {
            log.info("共同关注为空");
            return Collections.emptyList();
        }
        List<Long> list = commonFollowers.stream().map(Long::parseLong).collect(Collectors.toList());
        List<User> userList = userMapper.getList(list);
        return userList.stream().map(user -> UserVO.builder()
                .id(user.getId())
                .name(user.getName())
                .image(user.getImage())
                .address(user.getAddress())
                .fansCounts(user.getFansCounts())
                .followerCounts(user.getFollowerCounts())
                .status(user.getStatus())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional (rollbackFor = Exception.class)
    public Result recharge(rechargeDTO rechargeDTO) {
        try {
            BigDecimal amount = rechargeDTO.getAmount();
            if(amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error(MessageConstant.RECHARGE_AMOUNT_ERROR);
            }
            User user = userMapper.getById(BaseContext.getThreadLocal().getId());
            BigDecimal newMoney = user.getMoney().add(amount);
            user.setMoney(newMoney);
            userMapper.update(user);
            return Result.success(MessageConstant.RECHARGE_SUCCESS, newMoney);
        } catch (Exception e) {
            log.info("充值失败: {}", e.getMessage());
            return Result.error(MessageConstant.RECHARGE_FAIL);
        }
    }

}
