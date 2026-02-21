//package com.lghj.service;
//
//import com.lghj.pojo.entity.timescale.StockData;
//import com.lghj.pojo.entity.timescale.StockDataDaily;
//import com.lghj.pojo.entity.timescale.StockDataMonthly;
//import com.lghj.pojo.entity.timescale.StockDataWeekly;
//
//import java.util.List;
//
//public interface IStockDataService {
//
//    void saveDailyData(List<StockDataDaily> list);
//
//    void saveMonthlyData(List<StockDataMonthly> list);
//
//    void saveWeeklyData(List<StockDataWeekly> list);
//
//    /**
//     * 获取股市数据
//     *
//     * @param code 股票代码
//     * @param period D/M/Y
//     * @return
//     */
//    List<? extends StockData> queryStockData(String code, String period);
//}
