package com.wjh.controller.user;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.wjh.dto.UserLoginDTO;
import com.wjh.dto.UserRegisterDTO;
import com.wjh.result.Result;
import com.wjh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/send-code")
    public Result sendCode (@RequestBody UserLoginDTO userLoginDTO) {
        return userService.sendCode(userLoginDTO);
    }

    @PostMapping("/register")
    public Result register (@RequestBody UserRegisterDTO userRegisterDTO) {
        return userService.register(userRegisterDTO);
    }

    @PostMapping("/login/password")
    public Result loginByPassword (@RequestBody UserLoginDTO userLoginDTO) throws JsonProcessingException {
        return userService.loginByPassword(userLoginDTO);
    }

    @PostMapping("/login/code")
    public Result loginByCode (@RequestBody UserLoginDTO userLoginDTO) {
        return userService.loginByCode(userLoginDTO);
    }
}
