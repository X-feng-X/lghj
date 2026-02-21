package com.lghj.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟账户表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sim_account")
public class SimAccount implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("账户总资金")
    private BigDecimal totalCash;

    @ApiModelProperty("可用资金（总 - 冻结）")
    private BigDecimal availableCash;

    @ApiModelProperty("冻结资金（待成交买单）")
    private BigDecimal frozenCash;

    @ApiModelProperty("账户总资产（资金 + 持仓）")
    private BigDecimal totalAsset;

    @ApiModelProperty("版本号，用于乐观锁")
    @Version // mp乐观锁
    private Integer version;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("修改人")
    private String updateUser;

    @ApiModelProperty("是否已删除（0: 否，1: 是）")
    @TableLogic(value = "0", delval = "1")
    private Short isDeleted;
}
