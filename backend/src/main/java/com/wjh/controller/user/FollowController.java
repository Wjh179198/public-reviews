package com.wjh.controller.user;

import com.wjh.result.Result;
import com.wjh.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/follow/{userId}")
    public Result follow (@PathVariable Long userId) {
        return followService.follow(userId);
    }
}
