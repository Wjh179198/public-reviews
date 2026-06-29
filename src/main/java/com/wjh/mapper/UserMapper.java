package com.wjh.mapper;

import com.wjh.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User selectByParams(User user);

    @Insert("INSERT INTO user (phone, name, password, create_time, update_time) VALUES (#{phone}, #{name}, #{password}, #{createTime}, #{updateTime})")
    void insert(User user);
}
