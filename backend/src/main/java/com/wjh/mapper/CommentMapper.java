package com.wjh.mapper;

import com.wjh.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentMapper {

    void insert(Comment comment);

    List<Comment> getCommentsByPage(Long shopId, Integer lowScore, Integer highScore);

    @Select("SELECT * FROM comments WHERE id = #{commentId}")
    Comment getById(Long commentId);

    @Update("UPDATE comments SET likes = #{likes} WHERE id = #{commentId}")
    void updateLikes(Integer likes, Long commentId);
}
