package com.lghj.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A股基础信息表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("stock_basic")
public class StockBasic {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("股票代码")
    private String symbol;

    @ApiModelProperty("股票名称")
    private String name;

    @ApiModelProperty("股票简称")
    private String shortName;

    @ApiModelProperty("总股本")
    private Long totalShares;          // 总股本（股）

    @ApiModelProperty("流通股")
    private Long floatShares;           // 流通股（股）

    @ApiModelProperty("总市值")
    private Long totalMarketCap;        // 总市值（元）

    @ApiModelProperty("流通市值")
    private Long floatMarketCap;        // 流通市值（元）

    @ApiModelProperty("所属行业")
    private String industry;

    @ApiModelProperty("市场类型 (0-未知 1-沪A, 2-深A, 3-创业板, 4-科创板，5-北交所，6-新三版)")
    private Integer marketType;

    @ApiModelProperty("上市日期")
    private LocalDate listDate;

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