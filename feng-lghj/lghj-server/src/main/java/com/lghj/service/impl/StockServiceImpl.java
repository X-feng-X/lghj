package com.lghj.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.listener.StockExcelListener;
import com.lghj.listener.StockUpdateListener;
import com.lghj.mapper.StockBasicMapper;
import com.lghj.pojo.dto.StockExcel;
import com.lghj.pojo.entity.StockBasic;
import com.lghj.service.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Service
public class StockServiceImpl extends ServiceImpl<StockBasicMapper, StockBasic> implements IStockService {

    // 这里放你刚生成的 Excel 文件名
    private static final String EXCEL_PATH = "excel/A股详细数据.xlsx";

    /**
     * 导入A股基础信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importStockBasic() {
        try {
            log.info(">>>>> 开始读取 Excel 文件: {}", EXCEL_PATH);
            // 直接传入 this（当前 service 实例）给监听器
            StockExcelListener listener = new StockExcelListener(this);
            EasyExcel.read(EXCEL_PATH, StockExcel.class, listener).sheet().doRead();
            return "成功导入股票数据！";
        } catch (Exception e) {
            log.error("导入失败", e);
            return "导入失败: " + e.getMessage();
        }
    }

    /**
     * 分页查询股票列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键词（代码/名称）
     * @return 分页结果
     */
    @Override
    public Page<StockBasic> pageQuery(Integer pageNum, Integer pageSize, String keyword) {
        Page<StockBasic> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockBasic> wrapper = new LambdaQueryWrapper<>();

        // 只查询未删除的
        wrapper.eq(StockBasic::getIsDeleted, 0);

        // 关键词搜索（代码或名称）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(StockBasic::getSymbol, keyword)
                    .or()
                    .like(StockBasic::getName, keyword));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(StockBasic::getCreateTime);

        return this.page(page, wrapper);
    }

    /**
     * 根据股票代码更新基础信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByCode(StockBasic stockBasic) {
        // 根据 code 构造查询条件
        LambdaQueryWrapper<StockBasic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockBasic::getSymbol, stockBasic.getSymbol());
        // 执行更新（只会更新 stockBasic 中非空字段）
        return this.update(stockBasic, wrapper);
    }

    /**
     * 根据股票代码更新股票信息-批量更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateFromExcel() throws IOException {
        log.info("开始处理批量更新 Excel：{}", EXCEL_PATH);
        StockUpdateListener listener = new StockUpdateListener(this);
        EasyExcel.read(EXCEL_PATH, StockExcel.class, listener).sheet().doRead();
    }
}