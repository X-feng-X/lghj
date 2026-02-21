package com.lghj.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 委托单表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("trade_order")
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // 自动生成类的 equals() 和 hashCode() 方法
public class TradeOrder implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @EqualsAndHashCode.Include
    private Long id;

    @ApiModelProperty("委托单号（唯一标识）")
    private String orderNo;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("股票代码")
    private String symbol;

    @ApiModelProperty("买/卖（1-买，2-卖）")
    private Short direction;

    @ApiModelProperty("委托价格")
    private BigDecimal price;

    @ApiModelProperty("委托数量（股）")
    private Integer quantity;

    @ApiModelProperty("已成交数量")
    private Integer tradedQuantity;

    @ApiModelProperty("订单状态（1-待定，2-部分完成，3-已完成，4-已取消）")
    private Short status;

    @ApiModelProperty("撤销时间（未成交时）")
    private LocalDateTime cancelTime;

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
