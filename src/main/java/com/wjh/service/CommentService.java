package com.wjh.service;

import com.wjh.dto.CommentDTO;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.vo.CommentVO;

public interface CommentService {
    Result<CommentVO> createComment(CommentDTO commentDTO);

    Result<PageResult> getCommentList(Long shopId, String filter, Integer page, Integer pageSize);
}
