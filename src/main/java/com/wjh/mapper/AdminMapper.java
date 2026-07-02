package com.wjh.mapper;

import com.wjh.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper {

    @Select("select * from admin where name = #{name}")
    Admin getByName(String name);
}
