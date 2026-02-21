package com.lghj.pojo.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 简易用户DTO（评论/博客展示用）
 */
@Data
@Builder
public class UserBlogCommentsMessageDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名/昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;
}
