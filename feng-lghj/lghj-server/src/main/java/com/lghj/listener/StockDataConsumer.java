//package com.lghj.listener;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.lghj.pojo.entity.timescale.StockData;
//import com.lghj.service.IStockDataService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//@RocketMQMessageListener(
//        topic = "stock_data_topic", consumerGroup = "lghj-stock-consumer-group"
//)
//public class StockDataConsumer implements RocketMQListener<String> {
//
//    private final IStockDataService stockDataService;
//
//    @Override
//    public void onMessage(String message) {
//        log.info("Received stock data message: length={}", message.length());
//        try {
//            JSONObject jsonObject = JSON.parseObject(message);
//            String code = jsonObject.getString("code");
//            String type = jsonObject.getString("type");
//            JSONArray data = jsonObject.getJSONArray("data");
//
//            if (data == null || data.isEmpty()) {
//                log.warn("Empty data for code: {}", code);
//                return;
//            }
//
//            String period = mapTypeToPeriod(type);
//            List<StockData> stockDataList = new ArrayList<>();
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//            for (int i = 0; i < data.size(); i++) {
//                JSONObject item = data.getJSONObject(i);
//
//                // Map qstock fields to StockData
//                // qstock fields might be: date, open, high, low, close, volume, ...
//                // keys might vary, let's assume standard lower case english for now or handle variations
//
//                // 将股票字段映射到“股票数据”中
//                // 股票数据可能包括：日期、开盘价、最高价、最低价、收盘价、成交量等等。
//                // 密钥可能会有所不同，我们暂且假设使用标准的小写英文字母，或者对各种变体进行处理。
//
//                StockData stockData = new StockData();
//                stockData.setCode(code);
//                stockData.setPeriod(period);
//
//                // 日期处理
//                String dateStr = item.getString("date");
//                if (dateStr == null) dateStr = item.getString("Date");
//                if (dateStr != null) {
//                    // Assuming date is yyyy-MM-dd
//                    if (dateStr.length() == 10) {
//                        stockData.setTradeDate(LocalDateTime.parse(dateStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                    } else {
//                        // 尝试解析或者留空
//                    }
//                }
//
//                stockData.setOpen(item.getBigDecimal("open"));
//                stockData.setClose(item.getBigDecimal("close"));
//                stockData.setHigh(item.getBigDecimal("high"));
//                stockData.setLow(item.getBigDecimal("low"));
//
//                // Volume
//                if (item.containsKey("volume")) {
//                    stockData.setVolume(item.getLong("volume"));
//                } else if (item.containsKey("vol")) {
//                    stockData.setVolume(item.getLong("vol"));
//                }
//
//                // Amount
//                if (item.containsKey("amount")) {
//                    stockData.setAmount(item.getBigDecimal("amount"));
//                }
//
//                stockDataList.add(stockData);
//            }
//
//            log.info("Saving {} records for code {}", stockDataList.size(), code);
//            stockDataService.saveStockDataBatch(stockDataList);
//
//        } catch (Exception e) {
//            log.error("Error processing stock data message", e);
//        }
//    }
//
//    private String mapTypeToPeriod(String type) {
//        if ("daily".equalsIgnoreCase(type)) return "D";
//        if ("monthly".equalsIgnoreCase(type)) return "M";
//        if ("yearly".equalsIgnoreCase(type)) return "Y";
//        return "D"; // default
//    }
//}
