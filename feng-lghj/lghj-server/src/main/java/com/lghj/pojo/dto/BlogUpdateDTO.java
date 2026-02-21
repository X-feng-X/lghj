package com.lghj.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 博客编辑DTO：仅包含前端可编辑的字段
 */
@Data
public class BlogUpdateDTO {

    /**
     * 博客ID（必传，要知道编辑哪篇）
     */
    @NotNull(message = "博客ID不能为空")
    private Long id;

    /**
     * 博客标题
     */
    private String title;

    /**
     * 博客内容
     */
    private String content;

    /**
     * 照片，最多9张，多张以","隔开
     */
    private String images;
}