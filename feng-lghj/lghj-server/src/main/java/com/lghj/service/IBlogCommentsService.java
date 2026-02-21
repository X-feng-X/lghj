package com.lghj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lghj.pojo.dto.BlogCommentAddDTO;
import com.lghj.pojo.dto.BlogCommentQueryDTO;
import com.lghj.pojo.dto.PageResult;
import com.lghj.pojo.entity.BlogComments;

import java.util.List;

public interface IBlogCommentsService extends IService<BlogComments> {
    void addComment(BlogCommentAddDTO dto);

    PageResult queryCommentList(BlogCommentQueryDTO dto);

    void likeComment(Long commentId);

    void deleteComment(Long commentId);
}
