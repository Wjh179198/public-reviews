package com.wjh.controller.user;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.wjh.dto.UserLoginDTO;
import com.wjh.dto.UserRegisterDTO;
import com.wjh.dto.UserUpdateDTO;
import com.wjh.dto.rechargeDTO;
import com.wjh.entity.User;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.UserService;
import com.wjh.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/info")
    public Result<User> getInfo () {
        return userService.getInfo();
    }

    @PutMapping("/update")
    public Result<User> update (@RequestBody UserUpdateDTO userUpdateDTO) {
        User user = userService.update(userUpdateDTO);
        return Result.success(user);
    }

    @GetMapping("/search")
    public Result<List<UserVO>> searchUser (@RequestParam("keyword") String keyword) {
        List<UserVO> userVOList = userService.searchUser(keyword);
        return Result.success(userVOList);
    }

    @GetMapping("/{userId}")
    public Result<UserVO> getOtherUser (@PathVariable Long userId) {
        UserVO userVo = userService.getOtherUser(userId);
        return Result.success(userVo);
    }

    @GetMapping("/follow/check/{userId}")
    public Result<Boolean> checkIsFollow (@PathVariable Long userId) {
        Boolean isFollow = userService.checkIsFollow(userId);
        return Result.success(isFollow);
    }

    @GetMapping("/{userId}/fans")
    public Result<PageResult> fansList (@PathVariable Long userId, @RequestParam Integer page, @RequestParam Integer pageSize) {
        PageResult pageResult = userService.getFansList(userId, page, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/{userId}/followings")
    public Result<PageResult> followingsList (@PathVariable Long userId, @RequestParam Integer page, @RequestParam Integer pageSize) {
        PageResult pageResult = userService.getFollowingsList(userId, page, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/{userId}/common-follows")
    public Result<List<UserVO>> commonFollowsList (@PathVariable Long userId) {
        List<UserVO> userVOList = userService.getCommonFollowsList(userId);
        return Result.success(userVOList);
    }

    @PostMapping("/recharge")
    public Result recharge (@RequestBody rechargeDTO rechargeDTO) {
        return userService.recharge(rechargeDTO);
    }
}
