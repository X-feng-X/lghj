package com.lghj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lghj.pojo.dto.BlogUpdateDTO;
import com.lghj.pojo.dto.PageResult;
import com.lghj.pojo.entity.Blog;

import java.util.List;

public interface IBlogService extends IService<Blog> {
    void saveBlog(Blog blog);

    PageResult<Blog> queryBlogOfFollow(Integer pageNum, Integer pageSize);

    void likeBlog(Long id);

    List<Blog> queryHotBlog(Integer current, Integer size);

    Blog queryBlogById(Long id);

    void deleteMyBlog(Long id);

    void updateMyBlog(BlogUpdateDTO blogUpdateDTO);
}
