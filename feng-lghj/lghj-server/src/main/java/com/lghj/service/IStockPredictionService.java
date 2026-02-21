package com.lghj.service;

import com.lghj.pojo.vo.StockPredictionVO;

import javax.servlet.http.HttpServletResponse;

public interface IStockPredictionService {

    /**
     * 预测未来一个月股票价格
     *
     * @param symbol 股票代码
     * @return 预测结果
     */
    StockPredictionVO predictStock(String symbol);

    /**
     * 导出股票预测表格
     *
     * @param symbol 股票代码
     * @param response 响应对象
     */
    void excel(String symbol, HttpServletResponse response);
}
