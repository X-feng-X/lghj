package com.lghj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "股票预测结果VO")
public class StockPredictionVO implements Serializable {

    @ApiModelProperty("股票代码")
    private String symbol;

    @ApiModelProperty("预测列表")
    private List<PredictionItem> predictions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionItem implements Serializable {
        @ApiModelProperty("预测日期")
        private String date;

        @ApiModelProperty("预测价格")
        private BigDecimal price;
    }
}
