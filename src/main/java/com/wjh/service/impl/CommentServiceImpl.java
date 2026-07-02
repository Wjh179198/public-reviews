package com.wjh.service.impl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.context.BaseContext;
import com.wjh.dto.CommentDTO;
import com.wjh.entity.Comment;
import com.wjh.entity.Shop;
import com.wjh.entity.User;
import com.wjh.mapper.CommentMapper;
import com.wjh.mapper.ShopMapper;
import com.wjh.mapper.UserMapper;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.CommentService;
import com.wjh.vo.CommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<CommentVO> createComment(CommentDTO commentDTO) {
        Shop shop = shopMapper.getById(commentDTO.getShopId());
        if(shop == null) {
            return Result.error(MessageConstant.SHOP_NOT_EXISTS);
        }
        BigDecimal score = shop.getScore();
        Integer comments = shop.getComments();
        BigDecimal newScore = score.multiply(BigDecimal.valueOf(comments)).add(BigDecimal.valueOf(commentDTO.getScore())).divide(BigDecimal.valueOf(comments + 1), 2, BigDecimal.ROUND_HALF_UP);
        shop.setScore(newScore);
        shop.setComments(comments + 1);
        shopMapper.update(shop);
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);
        comment.setUserId(BaseContext.getThreadLocal().getId());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        String key = RedisConstant.SHOP_COMMENT_KEY + comment.getId();
        stringRedisTemplate.opsForSet().add(key, "0");
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment , commentVO);
        commentVO.setUserImage(BaseContext.getThreadLocal().getImage());
        commentVO.setUserName(BaseContext.getThreadLocal().getName());
        return Result.success("评论成功", commentVO);
    }

    @Override
    public Result<PageResult> getCommentList(Long shopId, String filter, Integer page, Integer pageSize) {
        Integer lowScore = 0;
        Integer highScore = 5;
        if("good".equals(filter)) {
            lowScore = 4;
        } else if ("mid".equals(filter)) {
            lowScore = 2;
            highScore = 3;
        } else if ("bad".equals(filter)) {
            highScore = 2;
        }
        PageHelper.startPage(page, pageSize);
        List<Comment> commentList = commentMapper.getCommentsByPage(shopId, lowScore, highScore);
        if(commentList == null || commentList.isEmpty()) {
            return Result.success(PageResult.builder().total(0L).records(new ArrayList<>()).pages(page).pageSize(pageSize).build());
        }
        Page<Comment> commentPage = (Page<Comment>) commentList;
        List<CommentVO> commentVOList = commentList.stream().map(comment -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            User user = userMapper.getById(comment.getUserId());
            commentVO.setUserName(user.getName());
            commentVO.setUserImage(user.getImage());
            return commentVO;
        }).collect(Collectors.toList());
        return Result.success(PageResult.builder()
                .total(commentPage.getTotal())
                .records(commentVOList)
                .pages(commentPage.getPages())
                .pageSize(commentPage.getPageSize())
                .build());
    }

    @Override
    public Result<Boolean> checkIsLike(Long commentId) {
        String key = RedisConstant.SHOP_COMMENT_KEY + commentId;
        Boolean member = stringRedisTemplate.opsForSet().isMember(key, BaseContext.getThreadLocal().getId().toString());
        return Result.success(member);
    }

    @Override
    public Result likeComment(Long commentId) {
        Comment comment = commentMapper.getById(commentId);
        if(comment == null) {
            return Result.error(MessageConstant.COMMENT_NOT_EXISTS);
        }
        String key = RedisConstant.SHOP_COMMENT_KEY + commentId;
        Boolean isLike = stringRedisTemplate.opsForSet().isMember(key, BaseContext.getThreadLocal().getId().toString());
        if(isLike) {
            stringRedisTemplate.opsForSet().remove(key, BaseContext.getThreadLocal().getId().toString());
            commentMapper.updateLikes(comment.getLikes() - 1, commentId);
        } else {
            stringRedisTemplate.opsForSet().add(key, BaseContext.getThreadLocal().getId().toString());
            commentMapper.updateLikes(comment.getLikes() + 1, commentId);
        }
        return Result.success();
    }
}
