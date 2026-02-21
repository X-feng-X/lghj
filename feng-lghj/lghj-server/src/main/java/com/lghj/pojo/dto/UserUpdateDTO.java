package com.lghj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 用户修改类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    @NotNull(message = "用户id不能为空")
    @ApiModelProperty(value = "用户id", required = true)
    private Long id;

    @ApiModelProperty(value = "登录用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

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

    @ApiModelProperty(value = "简化身份标识（1: 普通用户，2: 机构用户，3: 管理员）")
    private Short userType;

    @ApiModelProperty("状态（0: 禁用，1: 正常）")
    private Short status;

    @ApiModelProperty("管理员等级")
    private Integer roleName;
}
