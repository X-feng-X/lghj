package com.lghj.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson.JSON;
import com.lghj.pojo.dto.StockExcel;
import com.lghj.pojo.entity.StockBasic;
import com.lghj.service.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A股基础信息excel监听器
 */
@Slf4j
public class StockExcelListener implements ReadListener<StockExcel> {

    /**
     * 每隔100条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;
    /**
     * 缓存的数据
     */
    private List<StockBasic> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private final IStockService stockService;

    public StockExcelListener(IStockService stockService) {
        this.stockService = stockService;
    }

    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(StockExcel data, AnalysisContext context) {
        log.info("解析到一条数据:{}", JSON.toJSONString(data));
        // Excel DTO 转换为数据库实体
        StockBasic stockBasic = convertToEntity(data);
        cachedDataList.add(stockBasic);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 最后一批不足 BATCH_COUNT 的数据也保存
        if (!cachedDataList.isEmpty()) {
            saveData();
        }
        log.info("所有数据解析完成");
    }

    /**
     * 存储数据库
     */
    private void saveData() {
        log.info("{}条数据，开始存储数据库！", cachedDataList.size());
        stockService.saveBatch(cachedDataList); // 调用 MyBatis-Plus 的批量保存
        log.info("存储数据库成功！");
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