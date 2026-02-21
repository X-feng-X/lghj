package com.lghj.pojo.dto;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 新增博客评论DTO（一级/二级通用）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlogCommentAddDTO {

    /**
     * 关联博客ID（必传）
     */
    @NotNull(message = "博客ID不能为空")
    private Long blogId;

    /**
     * 父评论ID（一级评论=0，二级评论=一级评论ID，必传）
     */
    @NotNull(message = "父评论ID不能为空")
    private Long parentId = 0L;

    /**
     * 评论内容（必传，非空）
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
}