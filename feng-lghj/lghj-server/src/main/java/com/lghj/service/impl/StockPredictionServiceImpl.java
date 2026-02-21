package com.lghj.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson.JSON;
import com.lghj.constant.RedisConstant;
import com.lghj.pojo.dto.StockPredictionExcelDTO;
import com.lghj.pojo.vo.StockPredictionVO;
import com.lghj.service.IStockPredictionService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockPredictionServiceImpl implements IStockPredictionService {

    // Python prediction service URL
    // TODO 用网关
    private static final String PREDICTION_API_URL = "http://localhost:8001/predict/";
    private static final long EXPIRE_TIME = 24; // 过期时间，单位小时【一天】

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 预测未来一个月股票价格
     *
     * @param symbol 股票代码
     * @return 预测结果
     */
    @Override
    public StockPredictionVO predictStock(String symbol) {

        String key = RedisConstant.STOCK_PREDICTION + symbol;
        String res = stringRedisTemplate.opsForValue().get(key);

        if (res == null) {
            // Redis中没有获取到预测数据时，从python服务中重新预测
            String url = PREDICTION_API_URL + symbol;
            log.info("Calling prediction service: {}", url);
            try {
                String response = HttpUtil.get(url);
                if (StrUtil.isEmpty(response)) {
                    log.warn("预测结果返回为空：{}", symbol);
                    return null;
                }
                StockPredictionVO predictionVO = JSON.parseObject(response, StockPredictionVO.class);
                try {
                    stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(predictionVO), EXPIRE_TIME, TimeUnit.HOURS);
                    log.info("预测结缓存到Redis，股票代码：{}，过期时间：{}小时", symbol, EXPIRE_TIME);
                } catch (Exception e) {
                    log.error("预测结果缓存到Redis失败，股票代码：{}", symbol, e);
                }
                return predictionVO;
            } catch (Exception e) {
                log.error("预测失败：{}", symbol, e);
                return null;
            }
        }

        return JSON.parseObject(res, StockPredictionVO.class);
    }

    /**
     * 导出股票预测表格
     *
     * @param symbol 股票代码
     * @param response 响应对象
     */
    @Override
    public void excel(String symbol, HttpServletResponse response) {
        try {
            // 获取预测数据
            StockPredictionVO predictionVO = this.predictStock(symbol);
            if (predictionVO == null || predictionVO.getPredictions() == null) {
                log.warn("导出Excel失败，未获取到预测数据：{}", symbol);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString("未获取到预测数据"));
                return;
            }

            // 转换数据格式
            List<StockPredictionExcelDTO> dataList = new ArrayList<>();
            for (StockPredictionVO.PredictionItem item : predictionVO.getPredictions()) {
                dataList.add(StockPredictionExcelDTO.builder()
                        .date(item.getDate())
                        .price(item.getPrice())
                        .build());
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(symbol + "_预测数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), StockPredictionExcelDTO.class)
                    .sheet("预测结果")
                    .doWrite(dataList);

        } catch (Exception e) {
            log.error("导出Excel异常：{}", symbol, e);
            try {
                response.reset();
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString("导出失败"));
            } catch (Exception ex) {
                log.error("写出异常信息失败", ex);
            }
        }
    }

}
