package com.lghj.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.entity.Blog;
import com.lghj.service.IBlogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端-博客管理接口")
@Slf4j
@RestController
@RequestMapping("/api/admin/blog")
@RequiredArgsConstructor
public class BlogManageController {

    private final IBlogService blogService;

    /**
     * 分页查询博客列表
     */
    @GetMapping("/page")
    @ApiOperation("分页查询博客列表")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Blog> page = new Page<>(pageNum, pageSize);
        // 按创建时间倒序
        page = blogService.page(page);
        // 确保 records 不为 null
        if (page.getRecords() == null) {
            page.setRecords(List.of());
        }
        return Result.success(page);
    }

    /**
     * 删除博客
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除博客")
    public Result delete(@PathVariable Long id) {
        boolean success = blogService.removeById(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 查看博客详情
     */
    @GetMapping("/{id}")
    @ApiOperation("查看博客详情")
    public Result getById(@PathVariable Long id) {
        Blog blog = blogService.getById(id);
        return Result.success(blog);
    }
}
