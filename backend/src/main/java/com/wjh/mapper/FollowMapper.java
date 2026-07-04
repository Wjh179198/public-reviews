package com.wjh.mapper;

import com.wjh.entity.Follow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FollowMapper {

    @Delete("delete from follow where user_id = #{userId} and follow_user_id = #{followId}")
    int deleteByUserAndFollower(Long userId, Long followId);

    @Insert("insert ignore into follow(user_id, follow_user_id, create_time) values(#{userId}, #{followUserId}, #{createTime})")
    int insert(Follow follow);
}
