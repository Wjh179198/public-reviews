package com.wjh.mapper;

import com.wjh.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    void insert(Comment comment);

    List<Comment> getCommentsByPage(Long shopId, Integer lowScore, Integer highScore);
}
