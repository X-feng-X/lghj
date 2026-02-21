//package com.lghj.service.impl;
//
//import com.baomidou.dynamic.datasource.annotation.DS;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.lghj.mapper.StockDataDailyMapper;
//import com.lghj.mapper.StockDataMonthlyMapper;
//import com.lghj.mapper.StockDataWeeklyMapper;
//import com.lghj.pojo.entity.timescale.StockData;
//import com.lghj.pojo.entity.timescale.StockDataDaily;
//import com.lghj.pojo.entity.timescale.StockDataMonthly;
//import com.lghj.pojo.entity.timescale.StockDataWeekly;
//import com.lghj.service.IStockDataService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Collections;
//import java.util.List;
//
//@Service
//@DS("timescale")
//@RequiredArgsConstructor
//public class StockDataServiceImpl implements IStockDataService {
//
//    private final StockDataDailyMapper dailyMapper;
//    private final StockDataMonthlyMapper monthlyMapper;
//    private final StockDataWeeklyMapper weeklyMapper;
//
//    /**
//     * 保存日k数据
//     *
//     * @param list
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void saveDailyData(List<StockDataDaily> list) {
//        if (list != null && !list.isEmpty()) {
//            dailyMapper.upsertBatch(list);
//        }
//    }
//
//    /**
//     * 保存月k数据
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void saveWeeklyData(List<StockDataWeekly> list) {
//        if (list != null && !list.isEmpty()) {
//            weeklyMapper.upsertBatch(list);
//        }
//    }
//
//    /**
//     * 保存月k数据
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void saveMonthlyData(List<StockDataMonthly> list) {
//        if (list != null && !list.isEmpty()) {
//            monthlyMapper.upsertBatch(list);
//        }
//    }
//
//
//    /**
//     * 获取股市数据
//     *
//     * @param code   股票代码
//     * @param period D/W/M
//     * @return
//     */
//    @Override
//    public List<? extends StockData> queryStockData(String code, String period) {
//        if ("D".equalsIgnoreCase(period)) {
//            LambdaQueryWrapper<StockDataDaily> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(StockDataDaily::getCode, code).orderByAsc(StockDataDaily::getTradeDate);
//            return dailyMapper.selectList(wrapper);
//        } else if ("W".equalsIgnoreCase(period)) {
//            LambdaQueryWrapper<StockDataWeekly> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(StockDataWeekly::getCode, code).orderByAsc(StockDataWeekly::getTradeDate);
//            return weeklyMapper.selectList(wrapper);
//        } else if ("M".equalsIgnoreCase(period)) {
//            LambdaQueryWrapper<StockDataMonthly> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(StockDataMonthly::getCode, code).orderByAsc(StockDataMonthly::getTradeDate);
//            return monthlyMapper.selectList(wrapper);
//        }
//        return Collections.emptyList();
//
//
//        // TODO 存入timescaleDB数据库的方式，暂时不用
////        if ("D".equalsIgnoreCase(period)) {
////            LambdaQueryWrapper<StockDataDaily> wrapper = new LambdaQueryWrapper<>();
////            wrapper.eq(StockDataDaily::getCode, code).orderByAsc(StockDataDaily::getTradeDate);
////            return dailyMapper.selectList(wrapper);
////        } else if ("W".equalsIgnoreCase(period)) {
////            LambdaQueryWrapper<StockDataWeekly> wrapper = new LambdaQueryWrapper<>();
////            wrapper.eq(StockDataWeekly::getCode, code).orderByAsc(StockDataWeekly::getTradeDate);
////            return weeklyMapper.selectList(wrapper);
////        } else if ("M".equalsIgnoreCase(period)) {
////            LambdaQueryWrapper<StockDataMonthly> wrapper = new LambdaQueryWrapper<>();
////            wrapper.eq(StockDataMonthly::getCode, code).orderByAsc(StockDataMonthly::getTradeDate);
////            return monthlyMapper.selectList(wrapper);
////        }
////        return Collections.emptyList();
//    }
//}
