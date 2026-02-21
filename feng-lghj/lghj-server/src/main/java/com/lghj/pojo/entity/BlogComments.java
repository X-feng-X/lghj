package com.lghj.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("blog_comments")
public class BlogComments implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @ApiModelProperty("用户id")
    private Long userId;

    /**
     * 关联博客id
     */
    @ApiModelProperty("关联博客id")
    private Long blogId;

    /**
     * 关联的1级评论id，如果是一级评论，则值为0
     */
    @ApiModelProperty("关联的1级评论id，如果是一级评论，则值为0")
    private Long parentId;

    /**
     * 回复的内容
     */
    @ApiModelProperty("评论内容")
    private String content;

    /**
     * 点赞数
     */
    @ApiModelProperty("点赞数")
    private Integer liked;

    /**
     * 状态（0: 禁用，1: 正常）
     */
    @ApiModelProperty("状态（0: 禁用，1: 正常）")
    private Short status;


    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("修改人")
    private String updateUser;

    @ApiModelProperty("是否已删除（0: 否，1: 是）")
    private Short isDeleted;
}
