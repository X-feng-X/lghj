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
 * 用户持仓表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_position")
public class UserPosition implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("关联账户ID")
    private Long accountId;

    @ApiModelProperty("股票代码")
    private String symbol;

    @ApiModelProperty("总持仓数量")
    private Integer totalQuantity;

    @ApiModelProperty("冻结数量（待卖出）")
    private Integer frozenQuantity;

    @ApiModelProperty("可用数量（总 - 冻结）")
    private Integer availableQuantity;

    @ApiModelProperty("持仓成本价（平均）")
    private BigDecimal costPrice;

    @ApiModelProperty("浮盈浮亏（市值 - 成本）")
    private BigDecimal profitLoss;

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
