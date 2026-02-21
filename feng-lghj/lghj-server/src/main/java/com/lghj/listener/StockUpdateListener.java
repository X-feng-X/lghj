package com.lghj.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lghj.pojo.dto.StockExcel;
import com.lghj.pojo.entity.StockBasic;
import com.lghj.service.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StockUpdateListener implements ReadListener<StockExcel> {

    private final List<StockBasic> updateList = new ArrayList<>();
    private final IStockService stockService;
    private static final int BATCH_COUNT = 100;

    public StockUpdateListener(IStockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public void invoke(StockExcel data, AnalysisContext context) {
        // 转换为实体
        StockBasic entity = convertToEntity(data);
        updateList.add(entity);
        if (updateList.size() >= BATCH_COUNT) {
            batchUpdate();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!updateList.isEmpty()) {
            batchUpdate();
        }
        log.info("批量更新完成");
    }

    private void batchUpdate() {
        // 逐条更新（也可以使用批量更新方法，但 MyBatis-Plus 没有直接提供批量更新，可以自己写 SQL）
        for (StockBasic entity : updateList) {
            LambdaQueryWrapper<StockBasic> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StockBasic::getSymbol, entity.getSymbol());
            stockService.update(entity, wrapper);
        }
        log.info("批量更新 {} 条记录", updateList.size());
        updateList.clear();
    }

    /**
     * 字段映射
     */
    private StockBasic convertToEntity(StockExcel dto) {
        StockBasic stockBasic = new StockBasic();
        stockBasic.setSymbol(dto.getCode());
        stockBasic.setName(dto.getName());
        stockBasic.setShortName(dto.getShortName());
        stockBasic.setTotalShares(parseLong(dto.getTotalShares()));
        stockBasic.setFloatShares(parseLong(dto.getFloatShares()));
        stockBasic.setTotalMarketCap(parseLong(dto.getTotalMarketCap()));
        stockBasic.setFloatMarketCap(parseLong(dto.getFloatMarketCap()));
        stockBasic.setIndustry(dto.getIndustry());
        stockBasic.setMarketType(determineMarketType(dto.getCode()));
        stockBasic.setListDate(getListDate(dto.getListDateStr())); // 假设字段类型一致
        return stockBasic;
    }

    /**
     * 通过股票代码判断市场类型
     */
    private Integer determineMarketType(String code) {

        // 校验：必须是6位纯数字
        if (code == null || !code.matches("^\\d{6}$")) {
            return 0;
        }

        // 1-沪A（沪市主板）
        if (code.startsWith("600") || code.startsWith("601")
                || code.startsWith("603") || code.startsWith("605")) {
            return 1;
        }
        // 4-科创板
        if (code.startsWith("688")) {
            return 4;
        }
        // 2-深A（深市主板+中小板）
        if (code.startsWith("000") || code.startsWith("001")
                || code.startsWith("002") || code.startsWith("003")) {
            return 2;
        }
        // 3-创业板
        if (code.startsWith("300") || code.startsWith("301")) {
            return 3;
        }
        // 5-北交所（83/87/88开头）
        if (code.startsWith("83") || code.startsWith("87") || code.startsWith("88")) {
            return 5;
        }
        // 6-新三板（43开头）
        if (code.startsWith("43")) {
            return 6;
        }

        // 其他情况
        return 0;
    }

    /**
     * 转换上市时间
     */
    public LocalDate getListDate(String listDateStr) {
        if (listDateStr == null || listDateStr.isEmpty()) return null;
        // 假设格式为 yyyyMMdd，如 19910129
        return LocalDate.parse(listDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private BigDecimal parseBigDecimal(String str) {
        if (!StringUtils.hasText(str)) {
            return null;
        }
        try {
            // 去除可能的逗号、空格等
            String cleaned = str.replaceAll("[,\\s]", "");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("无法解析为 BigDecimal: {}", str);
            return null; // 或抛出异常，视业务需求
        }
    }

    private Long parseLong(String str) {
        if (!StringUtils.hasText(str)) {
            return null;
        }
        try {
            String cleaned = str.replaceAll("[,\\s]", "");
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            log.warn("无法解析为 Long: {}", str);
            return null;
        }
    }
}