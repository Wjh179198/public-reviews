package com.wjh.controller.admin;

import com.wjh.constant.RedisConstant;
import com.wjh.dto.AdminLoginDTO;
import com.wjh.entity.User;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.AdminService;
import com.wjh.vo.AdminVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/login")
    public Result login (@RequestBody AdminLoginDTO adminLoginDTO) {
        return adminService.login(adminLoginDTO);
    }

    @PostMapping("/logout")
    public Result logout (HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(token != null && !token.isEmpty()) {
            token = token.substring(7);
        }
        stringRedisTemplate.delete(RedisConstant.USER_LOGIN_KEY + token);
        return Result.success("已退出");
    }

    @GetMapping("/users")
    public Result<PageResult> UsersList (@RequestParam(required = false) Long id, @RequestParam(required = false) String name, @RequestParam(required = false) Integer status, @RequestParam Integer page, @RequestParam Integer pageSize) {
        return adminService.UsersList(id, name, status, page, pageSize);
    }

    @GetMapping("/user/{userId}")
    public Result<User> getUserById(@PathVariable Long userId) {
        return adminService.getUserById(userId);
    }

    @PostMapping("/user/ban/{userId}")
    public Result banUser (@PathVariable Long userId) {
        return adminService.banUser(userId);
    }

    @PostMapping("/user/unban/{userId}")
    public Result unbanUser (@PathVariable Long userId) {
        return adminService.unbanUser(userId);
    }

    @GetMapping("/info")
    public Result<AdminVO> getInfo () {
        return adminService.getInfo();
    }

    @PutMapping("/update")
    public Result updateAdmin (@RequestBody AdminLoginDTO adminLoginDTO) {
        return adminService.updateAdmin(adminLoginDTO);
    }

}
