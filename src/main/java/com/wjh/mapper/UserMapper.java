package com.wjh.mapper;

import com.wjh.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectByParams(User user);

    void insert(User user);

    void update(User user);

    List<User> getListByName(String name);

    @Select("select * from user where id = #{id}")
    User getById(Long id);
}
