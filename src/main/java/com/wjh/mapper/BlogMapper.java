package com.wjh.mapper;

import com.wjh.entity.Blog;
import com.wjh.vo.BlogVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BlogMapper {

    void insert(Blog blog);

    List<BlogVO> getBlogListAll();

    List<BlogVO> getBlogListFollowing(List<Long> followers);

    @Select("SELECT * FROM blog WHERE id = #{blogId}")
    Blog getById(Long blogId);

    @Update("UPDATE blog SET likes = #{likes} WHERE id = #{blogId}")
    void updateLikes(Integer likes, Long blogId);

    List<BlogVO> getUserLists(Long userId);
}
