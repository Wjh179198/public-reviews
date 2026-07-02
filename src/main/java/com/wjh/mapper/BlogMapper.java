package com.wjh.mapper;

import com.wjh.entity.Blog;
import com.wjh.vo.BlogVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BlogMapper {

    void insert(Blog blog);

    List<BlogVO> getBlogListAll();

    List<BlogVO> getBlogListFollowing(List<Long> followers);
}
