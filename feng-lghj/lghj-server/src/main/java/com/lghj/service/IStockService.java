package com.lghj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lghj.pojo.entity.StockBasic;

import java.io.IOException;

public interface IStockService extends IService<StockBasic> {

    String importStockBasic(); // 自定义导入方法

    boolean updateByCode(StockBasic stockBasic);

    void batchUpdateFromExcel() throws IOException;

    /**
     * 分页查询股票列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 关键词（代码/名称）
     * @return 分页结果
     */
    Page<StockBasic> pageQuery(Integer pageNum, Integer pageSize, String keyword);

}