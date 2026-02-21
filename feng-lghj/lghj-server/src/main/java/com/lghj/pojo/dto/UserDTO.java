package com.lghj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户传输类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @NotBlank(message = "用户名不能为空") // 只能校验字符串
    @ApiModelProperty(value = "登录用户名", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true)
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

    @NotNull(message = "身份标识不能为空") // 适用于所有类型（要求非 null）
    @ApiModelProperty(value = "简化身份标识（1: 普通用户，2: 机构用户，3: 管理员）", required = true)
    private Short userType;

    @ApiModelProperty("状态（0: 禁用，1: 正常）")
    private Short status;

    @ApiModelProperty("管理员等级")
    private Integer roleName;
}
