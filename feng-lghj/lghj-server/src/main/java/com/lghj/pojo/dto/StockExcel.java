package com.lghj.pojo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockExcel {

    @ExcelProperty("股票代码")
    private String code;

    @ExcelProperty("股票名称")
    private String name;

//    @ExcelProperty("最新")
//    private String latestPrice;

    @ExcelProperty("股票简称")
    private String shortName;

    @ExcelProperty("总股本")
    private String totalShares;          // 总股本（股）

    @ExcelProperty("流通股")
    private String floatShares;           // 流通股（股）

    @ExcelProperty("总市值")
    private String totalMarketCap;        // 总市值（元）

    @ExcelProperty("流通市值")
    private String floatMarketCap;        // 流通市值（元）

    @ExcelProperty("行业")
    private String industry;

    @ExcelProperty("上市时间")
    private String listDateStr;          // 先以字符串接收，如需日期可转换

}