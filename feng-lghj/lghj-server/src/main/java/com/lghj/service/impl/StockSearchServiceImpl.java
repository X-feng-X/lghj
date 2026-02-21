package com.lghj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lghj.mapper.StockBasicMapper;
import com.lghj.pojo.doc.StockDoc;
import com.lghj.pojo.entity.StockBasic;
import com.lghj.repository.StockSearchRepository;
import com.lghj.service.IStockSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockSearchServiceImpl implements IStockSearchService {

    private final StockBasicMapper stockBasicMapper;
    private final StockSearchRepository stockSearchRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 根据关键词搜索股票
     *
     * @param keyword 关键词（股票代码或名称）
     * @return 搜索结果列表
     */
    @Override
    public List<StockDoc> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 匹配 symbol (前缀匹配)
        boolQuery.should(QueryBuilders.prefixQuery("symbol", keyword));
        // 匹配 name (模糊匹配)
        boolQuery.should(QueryBuilders.matchQuery("name", keyword));
        // 匹配 industry (模糊匹配)
        boolQuery.should(QueryBuilders.matchQuery("industry", keyword));

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(0, 20)) // 限制返回前20条
                .build();

        SearchHits<StockDoc> searchHits = elasticsearchRestTemplate.search(searchQuery, StockDoc.class);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * 同步数据库数据到Elasticsearch
     */
    @Override
    public void syncData() {
        log.info("开始同步股票基础数据到Elasticsearch...");
        // 1. 从数据库查询所有股票基础信息
        List<StockBasic> stockList = stockBasicMapper.selectList(new LambdaQueryWrapper<StockBasic>()
                .eq(StockBasic::getIsDeleted, 0));

        if (stockList == null || stockList.isEmpty()) {
            log.warn("未查询到股票基础数据，同步结束");
            return;
        }

        // 2. 转换为文档对象
        List<StockDoc> docList = stockList.stream().map(stock -> StockDoc.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .industry(stock.getIndustry())
                .marketType(stock.getMarketType() != null ? String.valueOf(stock.getMarketType()) : "")
                .build()).collect(Collectors.toList());

        // 3. 批量保存到ES
        stockSearchRepository.saveAll(docList);
        log.info("成功同步 {} 条股票数据到Elasticsearch", docList.size());
    }
}
