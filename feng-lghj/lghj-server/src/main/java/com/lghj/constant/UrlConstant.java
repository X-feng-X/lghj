package com.lghj.constant;

public class UrlConstant {

    /**
     * 获取股票实时资讯url
     */
    public static final String STOCK_NEWS_URL = "https://search-api-web.eastmoney.com/search/jsonp";

    /**
     * 调用fastapi查询实时股市数据url
     */
    public static final String PREDICTION_API_MINUTE_URL = "http://localhost:8001/minute/{symbol}";

    /**
     * 新浪历史K线数据url
     */
    public static final String STOCK_HISTORY_SINA_KLINE_URL = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData";

    public static final String STOCK_HISTORY_TENCENT_KLINE_URL = "http://web.ifzq.gtimg.cn/appstock/app/kline/kline";
}
