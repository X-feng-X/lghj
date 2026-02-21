package com.lghj.controller.user;

import com.lghj.pojo.dto.Result;
import com.lghj.service.IRealTimeStockService;
import com.lghj.pojo.vo.StockNewsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/realtime")
@Api(tags = "实时股票数据接口")
@Slf4j
@RequiredArgsConstructor
public class RealTimeStockController {

    private final IRealTimeStockService realTimeStockService;

    @GetMapping("/news")
    @ApiOperation("获取股票实时资讯")
    public Result getStockNews(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "10") Integer recentN
    ) {
        log.info("获取股票实时资讯: symbol={}, recentN={}", symbol, recentN);
        List<StockNewsVO> data = realTimeStockService.getStockNews(symbol, recentN);
        return Result.success(data);
    }

    @GetMapping("/minute")
    @ApiOperation("获取股票分时数据")
    public Result getMinuteData(
            @RequestParam String market, // 市场代码：sh/sz
            @RequestParam String code // 股票代码
    ) {
        log.info("获取股票分时数据: market={}, code={}", market, code);
        List<Map<String, Object>> data = realTimeStockService.getMinuteData(market, code);
        return Result.success(data);
    }
}
