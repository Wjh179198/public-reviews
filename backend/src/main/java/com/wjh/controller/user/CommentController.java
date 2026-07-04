package com.wjh.controller.user;

import com.wjh.dto.CommentDTO;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.CommentService;
import com.wjh.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.StyledEditorKit;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/create")
    public Result<CommentVO> createComment (@RequestBody CommentDTO commentDTO) {
        return commentService.createComment(commentDTO);
    }

    @GetMapping("/list")
    public Result<PageResult> getCommentList(Long shopId, String filter, @RequestParam Integer page, @RequestParam Integer pageSize) {
        return commentService.getCommentList(shopId, filter, page, pageSize);
    }

    @GetMapping("/like/check/{commentId}")
    public Result<Boolean> checkIsLike (@PathVariable Long commentId) {
        return commentService.checkIsLike(commentId);
    }

    @PostMapping("/{commentId}/like")
    public Result likeComment (@PathVariable Long commentId) {
        return commentService.likeComment(commentId);
    }
}
