package com.lghj.controller.user;

import com.lghj.pojo.dto.BlogCommentAddDTO;
import com.lghj.pojo.dto.BlogCommentQueryDTO;
import com.lghj.pojo.dto.PageResult;
import com.lghj.pojo.dto.Result;
import com.lghj.service.IBlogCommentsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 博客评论区接口
 */
@Api(tags = "博客评论区接口")
@Slf4j
@RestController
@RequestMapping("/api/user/blog/comments")
@RequiredArgsConstructor // 自动生成包含所有 final 成员变量的构造器，简化代码
public class BlogCommentsController {

    private final IBlogCommentsService blogCommentsService;

    /**
     * 新增博客评论（一级/二级通用）
     */
    @PostMapping("/add")
    @ApiOperation("新增博客评论（一级/二级通用）")
    public Result addComment(@Valid @RequestBody BlogCommentAddDTO dto) {
        blogCommentsService.addComment(dto);
        return Result.success();
    }

    /**
     * 查询博客评论列表（带二级评论，树形结构）
     * GET /api/blog/comment/list?blogId=1&pageNum=1&pageSize=10
     */
    @GetMapping("/list")
    @ApiOperation("查询博客评论列表（带二级评论，树形结构）")
    public Result queryCommentList(@Valid BlogCommentQueryDTO dto) {
        // TODO 这个传参要是想规范点可以改一下
        PageResult blogCommentVOList = blogCommentsService.queryCommentList(dto);
        return Result.success(blogCommentVOList);
    }

    /**
     * 评论点赞/取消点赞
     */
    @PostMapping("/like/{commentId}")
    @ApiOperation("评论点赞/取消点赞")
    public Result likeComment(@PathVariable @NotNull(message = "评论ID不能为空") Long commentId) {
        blogCommentsService.likeComment(commentId);
        return Result.success();
    }

    /**
     * 删除评论（逻辑删除）
     */
    @DeleteMapping("/delete/{commentId}")
    @ApiOperation("删除评论（逻辑删除）")
    public Result deleteComment(@PathVariable @NotNull(message = "评论ID不能为空") Long commentId) {
        blogCommentsService.deleteComment(commentId);
        return Result.success();
    }
}
