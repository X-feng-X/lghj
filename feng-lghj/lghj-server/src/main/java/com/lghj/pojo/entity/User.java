package com.lghj.pojo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("`user`")
public class User implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("登录用户名")
    private String username;

    @ApiModelProperty("密码")
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

    @ApiModelProperty("简化身份标识（1: 普通用户、2: 机构用户、3: 管理员）")
    private Short userType;

    @ApiModelProperty("状态（0: 禁用、1: 正常）")
    private Short status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("修改人")
    private String updateUser;

    // 逻辑删除字段（标记是否删除：0=未删除，1=已删除）
    @ApiModelProperty("是否已删除（0: 否，1: 是）")
    @TableLogic(value = "0", delval = "1") // value：未删除值，delval：已删除值
    private Short isDeleted;
}
