package com.lghj.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("`blog`")
public class Blog {

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
     * 用户图标
     */
    @TableField(exist = false) // 表示当前字段不属于Blog表
    private String icon;

    /**
     * 用户昵称
     */
    @TableField(exist = false)
    private String name;

    /**
     * 是否点赞过了
     */
    @TableField(exist = false)
    private Boolean isLike;

    /**
     * 关联博客id
     */
    @ApiModelProperty("关联股票代码，多个用\",\"隔开")
    private String stockId;

    /**
     * 标题
     */
    @ApiModelProperty("标题")
    private String title;

    /**
     * 照片，最多9张，多张以","隔开
     */
    @ApiModelProperty("照片，最多9张，多张以\",\"隔开")
    private String images;

    /**
     * 正文
     */
    @ApiModelProperty("正文")
    private String context;

    /**
     * 点赞数
     */
    @ApiModelProperty("点赞数")
    private Integer liked;

    /**
     * 评论数
     */
    @ApiModelProperty("评论数")
    private Integer comments;

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
