//package com.lghj.task;
//
//import com.lghj.pojo.entity.timescale.StockDataDaily;
//import com.lghj.pojo.entity.timescale.StockDataMonthly;
//import com.lghj.pojo.entity.timescale.StockDataWeekly;
//import com.lghj.service.IStockDataService;
//import com.lghj.utils.SinaStockUtils;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.List;
//
///**
// * 获取股票日k、周k、月k数据定时任务
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StockSyncTask {
//
//    private final SinaStockUtils sinaStockUtils;
//    private final IStockDataService stockDataService;
//
//    // List of stocks to sync. In a real app, this should come from DB.
//    // TODO 目前先导入这几个，我的本地数据装不下
//    private static final List<String> STOCK_CODES = Arrays.asList("sz000001", "sh600519", "sh600036", "sz000858");
//
//    /**
//     * 每天在四点半同步股市数据 (Market closes at 15:00)
//     */
// TODO 先禁用，目前直接调用url接口实现
////    @Scheduled(cron = "0 30 16 * * ?")
//    public void syncDailyStockData() {
//        log.info("开启定期k线条数据同步...");
//
//        for (String code : STOCK_CODES) {
//            try {
//                log.info("正在同步以下日k数据: {}", code);
//                List<StockDataDaily> dailyList = sinaStockUtils.getDailyKLineData(code, 10);
//                if (!dailyList.isEmpty()) {
//                    stockDataService.saveDailyData(dailyList);
//                }
//
//                log.info("正在同步以下周k数据: {}", code);
//                List<StockDataWeekly> weeklyList = sinaStockUtils.getWeeklyKLineData(code, 10);
//                if (!dailyList.isEmpty()) {
//                    stockDataService.saveWeeklyData(weeklyList);
//                }
//
//                log.info("正在同步以下月k数据: {}", code);
//                List<StockDataMonthly> monthlyList = sinaStockUtils.getMonthlyKLineData(code, 2); // 查看过去两个月的情况
//                if (!monthlyList.isEmpty()) {
//                    stockDataService.saveMonthlyData(monthlyList);
//                }
//
//            } catch (Exception e) {
//                log.error("同步k线数据出错：" + code, e);
//            }
//        }
//
//        log.info("股票k线数据同步完成");
//    }
//
//    /**
//     * 初始同步助手 (can be triggered manually or once)
//     */
//    public void initialSync() {
//        log.info("Starting initial full sync...");
//        for (String code : STOCK_CODES) {
//            try {
//                // Daily: 1 year ~ 250 days
//                List<StockDataDaily> dailyList = sinaStockUtils.getDailyKLineData(code, 250);
//                if (!dailyList.isEmpty()) {
//                    stockDataService.saveDailyData(dailyList);
//                }
//
//                // Monthly: 5 years ~ 60 months
//                List<StockDataMonthly> monthlyList = sinaStockUtils.getMonthlyKLineData(code, 60);
//                if (!monthlyList.isEmpty()) {
//                    stockDataService.saveMonthlyData(monthlyList);
//                }
//            } catch (Exception e) {
//                log.error("Error initial syncing stock: " + code, e);
//            }
//        }
//    }
//}
