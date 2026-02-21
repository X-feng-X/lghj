package com.lghj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端用户信息展示VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    @ApiModelProperty("登录用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("电话号")
    private String phone;

    @ApiModelProperty("性别（1: 男，2: 女）")
    private String sex;

    @ApiModelProperty("简化身份标识（1: 普通用户，2: 机构用户，3: 管理员）")
    private String userType;

    @ApiModelProperty("状态（0: 禁用，1: 正常）")
    private String status;

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
