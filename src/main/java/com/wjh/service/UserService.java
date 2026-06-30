package com.wjh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wjh.dto.UserLoginDTO;
import com.wjh.dto.UserRegisterDTO;
import com.wjh.dto.UserUpdateDTO;
import com.wjh.entity.User;
import com.wjh.result.Result;

public interface UserService {
    Result sendCode(UserLoginDTO userLoginDTO);

    Result register(UserRegisterDTO userRegisterDTO);

    Result loginByPassword(UserLoginDTO userLoginDTO) throws JsonProcessingException;

    Result loginByCode(UserLoginDTO userLoginDTO);

    Result<User> getInfo();

    User update(UserUpdateDTO userUpdateDTO);
}
