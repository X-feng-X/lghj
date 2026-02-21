package com.lghj.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lghj.pojo.entity.timescale.StockDataWeekly;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
@DS("timescale")
public interface StockDataWeeklyMapper extends BaseMapper<StockDataWeekly> {
    void upsertBatch(List<StockDataWeekly> list);
}
