package com.wjh.controller.user;


import com.wjh.dto.UserLoginDTO;
import com.wjh.dto.UserRegisterDTO;
import com.wjh.result.Result;
import com.wjh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/send-code")
    public Result sendCode (UserLoginDTO userLoginDTO) {
        return userService.sendCode(userLoginDTO);
    }

    @PostMapping("/register")
    public Result register (UserRegisterDTO userRegisterDTO) {
        return userService.register(userRegisterDTO);
    }
}
