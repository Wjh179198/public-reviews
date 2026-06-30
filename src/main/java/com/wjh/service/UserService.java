package com.wjh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wjh.dto.UserLoginDTO;
import com.wjh.dto.UserRegisterDTO;
import com.wjh.dto.UserUpdateDTO;
import com.wjh.entity.User;
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

}
