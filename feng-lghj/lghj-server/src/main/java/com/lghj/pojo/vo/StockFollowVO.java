package com.lghj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "自选股VO")
public class StockFollowVO {

    @ApiModelProperty("股票ID")
    private Long stockId;

    @ApiModelProperty("股票代码")
    private String symbol;

    @ApiModelProperty("股票名称")
    private String name;

    @ApiModelProperty("最新价")
    private BigDecimal price;

    @ApiModelProperty("涨跌幅%")
    private BigDecimal changePercent;

    @ApiModelProperty("成交量")
    private Long volume;
}
