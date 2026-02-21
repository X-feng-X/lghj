package com.lghj.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lghj.pojo.entity.timescale.StockData;
import com.lghj.pojo.entity.timescale.StockDataDaily;
import com.lghj.pojo.entity.timescale.StockDataMonthly;
import com.lghj.pojo.entity.timescale.StockDataWeekly;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 查询股票k线数据工具类
 */
@Slf4j
@Component
public class SinaStockUtils {

    //    // private static final String KLINE_URL = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=%s&scale=%d&ma=no&datalen=%d";
    private static final String TENCENT_KLINE_URL = "http://web.ifzq.gtimg.cn/appstock/app/fqkline/get?param=%s,%s,,,%d,qfq";

    private static final String DAILY_PERIOD = "day";
    private static final String MONTHLY_PERIOD = "month";
    private static final String WEEKLY_PERIOD = "week";

    public List<StockDataDaily> getDailyKLineData(String code, int dataLen) {
        return getKLineData(code, DAILY_PERIOD, dataLen, StockDataDaily.class);
    }

    public List<StockDataMonthly> getMonthlyKLineData(String code, int dataLen) {
        log.debug("Fetching monthly K-line data for {} with datalen {}", code, dataLen);
        return getKLineData(code, MONTHLY_PERIOD, dataLen, StockDataMonthly.class);
    }

    public List<StockDataWeekly> getWeeklyKLineData(String code, int dataLen) {
        log.debug("Fetching weekly K-line data for {} with datalen {}", code, dataLen);
        return getKLineData(code, WEEKLY_PERIOD, dataLen, StockDataWeekly.class);
    }

    private <T extends StockData> List<T> getKLineData(String code, String period, int dataLen, Class<T> clazz) {
        // 腾讯接口需要将股票代码转换为特定格式，如sh600000
        String tencentCode = code.startsWith("sh") || code.startsWith("sz") ? code : "sh" + code;
        String url = String.format(TENCENT_KLINE_URL, tencentCode, period, dataLen);
        log.info("从腾讯接口获取股票数据: {}", url);

        try {
            String response = HttpUtil.get(url);
            if (response == null || response.isEmpty()) {
                log.warn("腾讯API返回空响应，股票代码: {}", code);
                return Collections.emptyList();
            }

            JSONObject jsonObject = JSON.parseObject(response);
            if (jsonObject == null) {
                log.warn("解析JSON响应失败，股票代码: {}，响应: {}", code, response);
                return Collections.emptyList();
            }

            // 腾讯接口返回格式：{"data":{"股票代码":{"day":[[日期,开盘,收盘,最高,最低,成交量,成交额],...]}}}
            JSONObject dataObject = jsonObject.getJSONObject("data");
            if (dataObject == null) {
                log.warn("响应中未找到数据，股票代码: {}", code);
                return Collections.emptyList();
            }

            JSONObject stockObject = dataObject.getJSONObject(tencentCode);
            if (stockObject == null) {
                log.warn("未找到股票数据，股票代码: {}", code);
                return Collections.emptyList();
            }

            JSONArray klineArray = stockObject.getJSONArray(period);
            if (klineArray == null) {
                log.warn("未找到K线数据，股票代码: {}，周期: {}", code, period);
                return Collections.emptyList();
            }

            List<T> list = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 0; i < klineArray.size(); i++) {
                JSONArray itemArray = klineArray.getJSONArray(i);
                if (itemArray.size() < 7) {
                    log.warn("K线数据格式无效，股票代码: {}，数据: {}", code, itemArray);
                    continue;
                }

                T stockData = clazz.getDeclaredConstructor().newInstance();
                stockData.setCode(code);

                String dateStr = itemArray.getString(0);
                if (dateStr != null) {
                    stockData.setTradeDate(LocalDate.parse(dateStr, formatter).atStartOfDay());
                }

                stockData.setOpen(new BigDecimal(itemArray.getString(1)));
                stockData.setClose(new BigDecimal(itemArray.getString(2)));
                stockData.setHigh(new BigDecimal(itemArray.getString(3)));
                stockData.setLow(new BigDecimal(itemArray.getString(4)));
                stockData.setVolume(itemArray.getLong(5));
                stockData.setAmount(new BigDecimal(itemArray.getString(6)));

                list.add(stockData);
            }
            return list;

        } catch (Exception e) {
            log.error("获取/解析股票数据失败，股票代码: " + code, e);
            return Collections.emptyList();
        }
    }
}
