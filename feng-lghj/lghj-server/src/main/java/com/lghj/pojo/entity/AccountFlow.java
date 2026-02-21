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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("account_flow")
public class AccountFlow implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("流水单号（唯一标识）")
    private String flowNo;

    @ApiModelProperty("关联用户ID")
    private Long userId;

    @ApiModelProperty("关联模拟账户ID")
    private Long accountId;

    @ApiModelProperty("流水类型：1-充值，2-提现，3-交易扣款（买入股票），4-交易回款（卖出股票），5-手续费扣减，6-系统调账")
    private Short flowType;

    @ApiModelProperty("资金变动金额：正数=增加，负数=减少")
    private BigDecimal amount;

    @ApiModelProperty("变动后账户可用余额（核心字段，用于对账）")
    private BigDecimal balanceAfter;

    @ApiModelProperty("关联单号：委托单号/成交单号/充值单号等，便于溯源")
    private String relatedNo;

    @ApiModelProperty("流水备注")
    private String remark;

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
