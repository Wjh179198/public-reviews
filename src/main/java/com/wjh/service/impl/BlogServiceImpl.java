package com.wjh.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.context.BaseContext;
import com.wjh.dto.BlogDTO;
import com.wjh.entity.Blog;
import com.wjh.entity.Shop;
import com.wjh.mapper.BlogMapper;
import com.wjh.mapper.ShopMapper;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.BlogService;
import com.wjh.vo.BlogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<BlogVO> createBlog(BlogDTO blogDTO) {
        Shop shop = shopMapper.getById(blogDTO.getShopId());
        if(shop == null) {
            return Result.error(MessageConstant.SHOP_NOT_EXISTS);
        }
        Blog blog = new Blog();
        BeanUtils.copyProperties(blogDTO, blog);
        blog.setUserId(BaseContext.getThreadLocal().getId());
        blog.setLikes(0);
        blog.setCreateTime(LocalDateTime.now());
        blog.setUpdateTime(LocalDateTime.now());
        blogMapper.insert(blog);
        String key = RedisConstant.BLOG_KEY + blog.getId();
        stringRedisTemplate.opsForSet().add(key, "0");
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
        blogVO.setUserName(BaseContext.getThreadLocal().getName());
        blogVO.setUserImage(BaseContext.getThreadLocal().getImage());
        blogVO.setShopName(shop.getName());
        return Result.success(MessageConstant.BLOG_CREATE_SUCCESS, blogVO);
    }

    @Override
    public Result<PageResult> getBlogList(String type, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<BlogVO> blogVOList = null;
        if("recommend".equals(type)) {
            blogVOList = blogMapper.getBlogListAll();
        } else if ("following".equals(type)) {
            String key = RedisConstant.USER_FOLLOW_KEY + BaseContext.getThreadLocal().getId().toString();
            Set<String> following = stringRedisTemplate.opsForSet().members(key);
            if(following == null || following.isEmpty()) {
                return Result.success(PageResult.builder().total(0L).records(new ArrayList<>()).pages(0).pageSize(pageSize).build());
            }
            List<Long> followers = following.stream().map(Long::parseLong).toList();
            blogVOList = blogMapper.getBlogListFollowing(followers);
        } else {
            return Result.error(MessageConstant.INVALID_TYPE);
        }
        Page<BlogVO> blogVOPage = (Page<BlogVO>) blogVOList;
        return Result.success(PageResult.builder()
                .total(blogVOPage.getTotal())
                .records(blogVOPage.getResult())
                .pages(blogVOPage.getPages())
                .pageSize(pageSize)
                .build());
    }

    @Override
    public Result<Boolean> checkLike(Long blogId) {
        String key = RedisConstant.BLOG_KEY + blogId;
        Set<String> keys = stringRedisTemplate.keys(key);
        if (keys == null || keys.isEmpty()) {
            return Result.error(MessageConstant.BLOG_NOT_EXISTS);
        }
        return Result.success(stringRedisTemplate.opsForSet().isMember(key, BaseContext.getThreadLocal().getId().toString()));
    }

    @Override
    public Result likeBlog(Long blogId) {
        Blog blog = blogMapper.getById(blogId);
        if(blog == null) {
            return Result.error(MessageConstant.BLOG_NOT_EXISTS);
        }
        String key = RedisConstant.BLOG_KEY + blogId;
        String userId = BaseContext.getThreadLocal().getId().toString();
        Boolean isLike = stringRedisTemplate.opsForSet().isMember(key, userId);
        if(isLike) {
            stringRedisTemplate.opsForSet().remove(key, userId);
            Integer likes = blog.getLikes();
            blogMapper.updateLikes(likes - 1, blogId);
        } else {
            stringRedisTemplate.opsForSet().add(key, userId);
            Integer likes = blog.getLikes();
            blogMapper.updateLikes(likes + 1, blogId);
        }
        return Result.success();
    }

    @Override
    public Result<PageResult> getUserBlogs(Long userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<BlogVO> blogVOList = blogMapper.getUserLists(userId);
        if(blogVOList == null || blogVOList.isEmpty()) {
            return Result.success(PageResult.builder().total(0L).records(new ArrayList<>()).pages(0).pageSize(pageSize).build());
        }
        Page<BlogVO> blogVOPage = (Page<BlogVO>) blogVOList;
        return Result.success(PageResult.builder()
                .total(blogVOPage.getTotal())
                .records(blogVOPage.getResult())
                .pages(blogVOPage.getPages())
                .pageSize(pageSize)
                .build());
    }

    @Override
    public Result deleteBlog(Long blogId) {
        Blog blog = blogMapper.getById(blogId);
        if(blog == null) {
            return Result.error(MessageConstant.BLOG_NOT_EXISTS);
        }
        Long userId = blog.getUserId();
        if(!userId.equals(BaseContext.getThreadLocal().getId())) {
            return Result.error(MessageConstant.NO_PERMISSION_DELETE_BLOG);
        }
        blogMapper.deleteBlog(blogId);
        return Result.success(MessageConstant.BLOG_DELETE_SUCCESS);
    }
}
