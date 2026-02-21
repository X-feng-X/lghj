package com.lghj.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.entity.BlogComments;
import com.lghj.service.IBlogCommentsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端-博客评论管理接口")
@Slf4j
@RestController
@RequestMapping("/api/admin/blog/comments")
@RequiredArgsConstructor
public class BlogCommentsManageController {

    private final IBlogCommentsService blogCommentsService;

    /**
     * 分页查询评论列表
     */
    @GetMapping("/page")
    @ApiOperation("分页查询评论列表")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Long blogId) {
        Page<BlogComments> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BlogComments> wrapper = new LambdaQueryWrapper<>();
        
        if (blogId != null) {
            wrapper.eq(BlogComments::getBlogId, blogId);
        }
        // 按创建时间倒序
        wrapper.orderByDesc(BlogComments::getCreateTime);
        
        page = blogCommentsService.page(page, wrapper);
        
        if (page.getRecords() == null) {
            page.setRecords(List.of());
        }
        
        return Result.success(page);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除评论")
    public Result delete(@PathVariable Long id) {
        boolean success = blogCommentsService.removeById(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}
