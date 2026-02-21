package com.lghj.controller.user;

import com.lghj.enums.ErrorEnum;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.vo.StockPredictionVO;
import com.lghj.service.IStockPredictionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/user/prediction")
@Api(tags = "股票预测接口")
@Slf4j
@RequiredArgsConstructor
public class StockPredictionController {

    private final IStockPredictionService stockPredictionService;

    @GetMapping("/{symbol}")
    @ApiOperation("预测股票未来一个月价格")
    public Result predict(@PathVariable String symbol) {
        log.info("获取股票预测结果: {}", symbol);
        StockPredictionVO vo = stockPredictionService.predictStock(symbol);
        if (vo == null) {
            return Result.error("预测服务不可用或无数据");
        }
        return Result.success(vo);
    }

    @GetMapping("/{symbol}/get_excel")
    @ApiOperation("导出excel表格")
    public void excel(@PathVariable String symbol, HttpServletResponse response) { // HttpServletResponse response 告诉浏览器："这是一个需要下载的文件"（设置响应头）
        log.info("导出股票预测结果表格：{}", symbol);
        stockPredictionService.excel(symbol, response);
    }
}
