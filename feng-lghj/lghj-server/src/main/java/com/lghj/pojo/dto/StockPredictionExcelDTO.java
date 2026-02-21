package com.lghj.pojo.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockPredictionExcelDTO {

    @ExcelProperty("预测日期")
    private String date;

    @ExcelProperty("预测价格")
    private BigDecimal price;
}
