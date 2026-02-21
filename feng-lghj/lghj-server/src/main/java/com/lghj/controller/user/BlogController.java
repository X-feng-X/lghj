package com.lghj.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lghj.context.BaseContext;
import com.lghj.pojo.dto.BlogUpdateDTO;
import com.lghj.pojo.dto.PageResult;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.entity.Blog;
import com.lghj.service.IBlogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 讨论区博客
 */
@Api(tags = "用户讨论区博客接口")
@Slf4j
@RestController
@RequestMapping("/api/user/blog")
@RequiredArgsConstructor // 自动生成包含所有 final 成员变量的构造器，简化代码
public class BlogController {

    private final IBlogService blogService;

    /**
     * 发布博客
     */
    @PostMapping
    @ApiOperation("发布博客")
    public Result saveBlog(@RequestBody Blog blog) {
        blogService.saveBlog(blog);
        return Result.success();
    }

    /**
     * 点赞功能接口
     */
    @PutMapping("/like/{id}")
    @ApiOperation("点赞功能接口")
    public Result likeBlog(@PathVariable("id") Long id) {
        blogService.likeBlog(id);
        return Result.success();
    }

    /**
     * 分页查看登录用户自己的博客内容
     */
    @GetMapping("/query/of/me")
    @ApiOperation("分页查看登录用户自己的博客内容")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current,
                              @RequestParam(value = "size", defaultValue = "10") Integer size) {
        // 获取登录用户
        Long userId = BaseContext.getCurrentId();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", userId).page(new Page<>(current, size));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.success(records);
    }

    /**
     * 根据点赞数量（热度）展示博客
     */
    @GetMapping("/query/hot")
    @ApiOperation("根据点赞数量（热度）展示博客")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        List<Blog> blogList = blogService.queryHotBlog(current, size);
        return Result.success(blogList);
    }

    /**
     * 根据id查询博客
     */
    @GetMapping("/query/{id}")
    @ApiOperation("根据id查询博客")
    public Result queryBlogById(@PathVariable("id") Long id) {
        Blog blog = blogService.queryBlogById(id);
        return Result.success(blog);
    }


    /**
     * 查看指定用户发的博客
     */
    @GetMapping("/query/of/user")
    @ApiOperation("查看指定用户发的博客")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam("id") Long id) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, size));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.success(records);
    }

    /**
     * 粉丝查看关注所有用户博客接口
     */
    @GetMapping("/query/of/follow")
    @ApiOperation("粉丝查看关注所有用户博客接口")
    public Result queryBlogOfFollow(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {

        PageResult<Blog> blogOfFollow = blogService.queryBlogOfFollow(current, size);
        return Result.success(blogOfFollow);
    }

    /**
     * 用户删除自己的博客
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("用户删除自己的博客")
    public Result deleteMyBlog(@PathVariable Long id) {
        blogService.deleteMyBlog(id);
        return Result.success();
    }

    /**
     * 用户编辑自己的博客
     */
    @PutMapping("/update")
    @ApiOperation("用户编辑自己的博客")
    public Result updateMyBlog(@Valid @RequestBody BlogUpdateDTO blogUpdateDTO) {
        blogService.updateMyBlog(blogUpdateDTO);
        return Result.success();
    }
}
