package com.lghj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 登录返回 VO（包含用户身份信息）
 */
@Data
@Builder
public class LoginVO {
    // JWT 令牌（后续接口权限校验用，企业开发必备）
    @ApiModelProperty(value = "JWT 令牌")
    private String token;

    // 用户主键 ID
    @ApiModelProperty(value = "用户主键ID")
    private Long id;

    // 用户名
    @ApiModelProperty(value = "用户名")
    private String username;

    // 身份类型
    @ApiModelProperty(value = "身份类型")
    private Short userType;

    // 身份描述（方便前端展示："普通用户"/"机构用户"/"管理员"）
    @ApiModelProperty(value = "身份描述")
    private String identityDesc;

    // 账号状态（0=禁用，1=正常）
    @ApiModelProperty(value = "账号状态")
    private Short state;
}