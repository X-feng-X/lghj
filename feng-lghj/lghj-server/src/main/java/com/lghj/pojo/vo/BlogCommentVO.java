package com.lghj.pojo.vo;

import com.lghj.pojo.dto.UserBlogCommentsMessageDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 博客评论展示VO（含发布者信息+点赞状态+二级评论）
 */
@Data
public class BlogCommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论发布者信息
     */
    private UserBlogCommentsMessageDTO user;

    /**
     * 父评论ID（一级=0）
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数量
     */
    private Integer liked;

    /**
     * 当前登录用户是否点赞（1=是，0=否；游客为0）
     */
    private Integer isLiked = 0;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 二级评论列表（一级评论专属，二级评论该字段为null）
     */
    private List<BlogCommentVO> children;
}