package com.lghj.repository;

import com.lghj.pojo.doc.StockDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockSearchRepository extends ElasticsearchRepository<StockDoc, Long> {

}
