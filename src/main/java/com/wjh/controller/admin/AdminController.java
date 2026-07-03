package com.wjh.controller.admin;

import com.wjh.constant.RedisConstant;
import com.wjh.dto.AdminLoginDTO;
import com.wjh.entity.User;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result login (@RequestBody AdminLoginDTO adminLoginDTO) {
        return adminService.login(adminLoginDTO);
    }

    @GetMapping("/users")
    public Result<PageResult> UsersList (@RequestParam(required = false) Long id, @RequestParam(required = false) String name, @RequestParam(required = false) Integer status, @RequestParam Integer page, @RequestParam Integer pageSize) {
        return adminService.UsersList(id, name, status, page, pageSize);
    }

    @GetMapping("/user/{userId}")
    public Result<User> getUserById(@PathVariable Long userId) {
        return adminService.getUserById(userId);
    }

    @GetMapping("/user/ban/{userId}")
    public Result banUser (@PathVariable Long userId) {
        return adminService.banUser(userId);
    }
}
