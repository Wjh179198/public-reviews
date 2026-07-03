package com.wjh.service;

import com.wjh.dto.AdminLoginDTO;
import com.wjh.entity.User;
import com.wjh.result.PageResult;
import com.wjh.result.Result;

public interface AdminService {

    Result login(AdminLoginDTO adminLoginDTO);

    Result<PageResult> UsersList(Long id, String name, Integer status, Integer page, Integer pageSize);

    Result<User> getUserById(Long userId);

    Result banUser(Long userId);
}
