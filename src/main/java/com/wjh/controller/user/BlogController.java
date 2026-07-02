package com.wjh.controller.user;

import com.wjh.dto.BlogDTO;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.BlogService;
import com.wjh.vo.BlogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @PostMapping("/create")
    public Result<BlogVO> createBlog (@RequestBody BlogDTO blogDTO) {
        return blogService.createBlog(blogDTO);
    }

    @GetMapping("/list")
    public Result<PageResult> getBlogList (@RequestParam String type, @RequestParam Integer page, @RequestParam Integer pageSize) {
        return blogService.getBlogList(type, page, pageSize);
    }

}
