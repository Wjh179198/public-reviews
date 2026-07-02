package com.wjh.service;

import com.wjh.dto.BlogDTO;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.vo.BlogVO;

public interface BlogService {

    Result<BlogVO> createBlog(BlogDTO blogDTO);

    Result<PageResult> getBlogList(String type, Integer page, Integer pageSize);

    Result<Boolean> checkLike(Long blogId);

}
