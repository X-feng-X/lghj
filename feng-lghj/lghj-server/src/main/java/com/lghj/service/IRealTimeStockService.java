package com.lghj.service;

import com.lghj.pojo.vo.StockNewsVO;

import java.util.List;
import java.util.Map;

public interface IRealTimeStockService {

    /**
     * 获取股票实时资讯
     *
     * @param symbol  股票代码
     * @param recentN 获取最近N条
     * @return 新闻列表
     */
    List<StockNewsVO> getStockNews(String symbol, int recentN);

    /**
     * 获取股票分时数据
     *
     * @param market 市场代码（sh/sz）
     * @param code   股票代码
     * @return 分时数据列表，每个元素包含时间、价格、成交量等信息
     */
    List<Map<String, Object>> getMinuteData(String market, String code);

    /**
     * 从Redis获取缓存的分时数据
     *
     * @param market 市场代码
     * @param code   股票代码
     * @return 分时数据列表
     */
    List<Map<String, Object>> getCachedMinuteData(String market, String code);

    /**
     * 将分时数据缓存到Redis
     *
     * @param market 市场代码
     * @param code   股票代码
     * @param data   分时数据列表
     */
    void cacheMinuteData(String market, String code, List<Map<String, Object>> data);

    /**
     * 获取股票实时行情（包含最新价等）
     *
     * @param market 市场代码（如 "sh", "sz"）
     * @param code   股票代码
     * @return 行情数据 Map，包含最新价、涨跌幅等，失败返回 null
     */
    Map<String, Object> getRealTimeQuote(String market, String code);
}
