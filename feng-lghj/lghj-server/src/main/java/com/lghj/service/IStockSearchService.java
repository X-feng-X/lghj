package com.lghj.service;

import com.lghj.pojo.doc.StockDoc;
import java.util.List;

public interface IStockSearchService {

    /**
     * 根据关键词搜索股票
     * @param keyword 关键词（股票代码或名称）
     * @return 搜索结果列表
     */
    List<StockDoc> search(String keyword);

    /**
     * 同步数据库数据到Elasticsearch
     */
    void syncData();
}
