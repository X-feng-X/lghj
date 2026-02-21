package com.lghj.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lghj.pojo.entity.timescale.StockDataDaily;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
@DS("timescale")
public interface StockDataDailyMapper extends BaseMapper<StockDataDaily> {

    void upsertBatch(List<StockDataDaily> list);
}
