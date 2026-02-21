package com.lghj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户查询参数类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryDTO {

    @ApiModelProperty("登录用户名")
    private String username;

    @ApiModelProperty("昵称")
    private String nickName;

    @ApiModelProperty("头像")
    private String icon;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("电话号")
    private String phone;

    @ApiModelProperty("性别（1: 男，2: 女）")
    private Short sex;

    @ApiModelProperty("简化身份标识（1: 普通用户，2: 机构用户，3: 管理员）")
    private Short userType;

    @ApiModelProperty("状态（0: 禁用、1: 正常）")
    private Short status;

    @ApiModelProperty("搜索起始时间")
    private LocalDateTime begin;

    @ApiModelProperty("搜索终止时间")
    private LocalDateTime end;

    @ApiModelProperty("是否已删除（0: 否，1: 是）")
    private Short isDeleted;

}
