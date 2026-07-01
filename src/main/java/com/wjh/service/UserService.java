package com.wjh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wjh.dto.UserLoginDTO;
import com.wjh.dto.UserRegisterDTO;
import com.wjh.dto.UserUpdateDTO;
import com.wjh.dto.rechargeDTO;
import com.wjh.entity.User;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.vo.UserVO;

import java.util.List;

public interface UserService {
    Result sendCode(UserLoginDTO userLoginDTO);

    Result register(UserRegisterDTO userRegisterDTO);

    Result loginByPassword(UserLoginDTO userLoginDTO) throws JsonProcessingException;

    Result loginByCode(UserLoginDTO userLoginDTO);

    Result<User> getInfo();

    User update(UserUpdateDTO userUpdateDTO);

    List<UserVO> searchUser(String keyword);

    UserVO getOtherUser(Long userId);

    Boolean checkIsFollow(Long userId);

    PageResult getFansList(Long userId, Integer page, Integer pageSize);

    PageResult getFollowingsList(Long userId, Integer page, Integer pageSize);

    List<UserVO> getCommonFollowsList (Long userId);

    Result recharge(rechargeDTO rechargeDTO);

}
