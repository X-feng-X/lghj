//package com.lghj.controller.user;
//
//import com.lghj.pojo.entity.timescale.StockData;
//import com.lghj.pojo.dto.Result;
//import com.lghj.service.IStockDataService;
//import com.lghj.task.StockSyncTask;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
// TODO 暂时弃用，用python的akshare库实现，后续将数据存入timescaleDB后再重新完善
//@RestController
//@RequestMapping("/api/user/stock")
//@Api(tags = "股市数据展示接口")
//@Slf4j
//@RequiredArgsConstructor
//public class StockDataController {
//
//    private final IStockDataService stockDataService;
//    private final StockSyncTask stockSyncTask;
//
//    @GetMapping("/sync")
//    @ApiOperation("手动同步")
//    public Result triggerSync() {
//        stockSyncTask.syncDailyStockData();
//        return Result.success();
//    }
//
//    @GetMapping("/data")
//    @ApiOperation("查看股市数据")
//    public Result getStockData(
//            @RequestParam String code,
//            @RequestParam(defaultValue = "D") String period) {
//        log.info("查看股市数据: code={}, period={}", code, period);
//        List<? extends StockData> list = stockDataService.queryStockData(code, period);
//        return Result.success(list);
//    }
//}
