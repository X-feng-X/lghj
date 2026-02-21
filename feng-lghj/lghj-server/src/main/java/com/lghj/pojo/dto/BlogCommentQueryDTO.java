package com.lghj.pojo.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 博客评论查询DTO
 */
@Data
public class BlogCommentQueryDTO {

    /**
     * 博客ID（必传）
     */
    @NotNull(message = "博客ID不能为空")
    private Long blogId;

    /**
     * 当前页码（默认1）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数（默认10，一级评论分页）
     */
    private Integer pageSize = 10;
}