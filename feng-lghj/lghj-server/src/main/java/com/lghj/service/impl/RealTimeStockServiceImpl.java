package com.lghj.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lghj.constant.RedisConstant;
import com.lghj.constant.StockConstant;
import com.lghj.constant.UrlConstant;
import com.lghj.service.IRealTimeStockService;
import com.lghj.pojo.vo.StockNewsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimeStockServiceImpl implements IRealTimeStockService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RestTemplate restTemplate;

    private static final String REDIS_KEY_PREFIX = "stock:minute:";
    private static final long EXPIRE_TIME = 24; // 过期时间，单位小时
    // 缓存刷新时间（秒），小于这个时间则认为是新鲜数据，不需要刷新
    private static final long REFRESH_INTERVAL = 60;

    /**
     * 获取股票实时资讯
     *
     * @param symbol  股票代码
     * @param recentN 获取最近N条
     * @return 新闻列表
     */
    @Override
    public List<StockNewsVO> getStockNews(String symbol, int recentN) {

        // TODO 可以考虑存Redis
        // 构造内部参数
        Map<String, Object> innerParam = new HashMap<>();
        innerParam.put("uid", "");
        innerParam.put("keyword", symbol);
        innerParam.put("type", new String[]{"cmsArticleWebOld"});
        innerParam.put("client", "web");
        innerParam.put("clientType", "web");
        innerParam.put("clientVersion", "curr");

        Map<String, Object> cmsParam = new HashMap<>();
        cmsParam.put("searchScope", "default");
        cmsParam.put("sort", "default");
        cmsParam.put("pageIndex", 1);
        cmsParam.put("pageSize", recentN);
        cmsParam.put("preTag", "");
        cmsParam.put("postTag", "");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cmsArticleWebOld", cmsParam);
        innerParam.put("param", paramMap);

        String jsonParam = JSON.toJSONString(innerParam);
        String cb = "jQuery35101792940631092459_" + System.currentTimeMillis();

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("cb", cb);
        requestParams.put("param", jsonParam);
        requestParams.put("_", System.currentTimeMillis());

        try {
            String response = HttpUtil.createGet(UrlConstant.STOCK_NEWS_URL)
                    .form(requestParams)
                    .header("Referer", "https://so.eastmoney.com/news/s?keyword=" + symbol)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
                    .execute()
                    .body();

            if (response != null && response.contains("(")) {
                String jsonStr = response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"));
                JSONObject jsonObject = JSON.parseObject(jsonStr);
                if (jsonObject != null && jsonObject.containsKey("result")) {
                    JSONObject result = jsonObject.getJSONObject("result");
                    if (result != null && result.containsKey("cmsArticleWebOld")) {
                        JSONArray articles = result.getJSONArray("cmsArticleWebOld");
                        List<StockNewsVO> newsList = new ArrayList<>();
                        for (int i = 0; i < articles.size(); i++) {
                            JSONObject article = articles.getJSONObject(i);
                            StockNewsVO vo = new StockNewsVO();
                            vo.setKeyword(symbol);
                            vo.setTitle(cleanText(article.getString("title")));
                            vo.setContent(cleanText(article.getString("content")));
                            vo.setPublishTime(article.getString("date"));
                            vo.setSource(article.getString("mediaName"));

                            String code = article.getString("code");
                            if (code != null && !code.isEmpty()) {
                                vo.setUrl("http://finance.eastmoney.com/a/" + code + ".html");
                            } else {
                                vo.setUrl(article.getString("url"));
                            }

                            newsList.add(vo);
                        }
                        return newsList;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取股票新闻异常，股票代码：{}", symbol, e);
        }
        return new ArrayList<>();
    }

    /**
     * 用于对输入的字符串进行清洗和格式化处理
     * 空值检查、字符串替换、去空格
     *
     * @param text 输入的字符串
     * @return 清洗后的数据
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("<em>", "").replace("</em>", "")
                .replace("&nbsp;", " ").trim();
    }

    /**
     * 获取股票分时数据 优化逻辑： 1. 优先从 Redis 获取缓存 2. 如果缓存存在且未过期（判断数据更新时间），直接返回 3.
     * 如果缓存不存在或已过期，获取分布式锁 4. 拿到锁的线程去调用 Python 服务更新数据，并更新 Redis 5.
     * 没拿到锁的线程，等待或者直接返回旧数据（如果存在）
     */
    @Override
    public List<Map<String, Object>> getMinuteData(String market, String code) {

        String redisKey = REDIS_KEY_PREFIX + market + code;

        // 1. 尝试从 Redis 获取数据
        List<Map<String, Object>> cachedData = getCachedMinuteData(market, code);

        // 判断是否需要刷新数据
        boolean needRefresh = false;
        if (cachedData == null || cachedData.isEmpty()) {
            needRefresh = true;
        } else {
            // 检查数据的新鲜度（这里假设缓存中存了 updateTime，或者简单通过过期时间判断）
            // 为了简化，我们使用一个辅助的 key 存储最后更新时间
            String updateTimeKey = "stock:minute:updatetime:" + market + code;
            String lastUpdateTimeStr = stringRedisTemplate.opsForValue().get(updateTimeKey);
            long lastUpdateTime = lastUpdateTimeStr != null ? Long.parseLong(lastUpdateTimeStr) : 0;

            // 如果距离上次更新超过60秒，且在交易时间内（这里简化为总是刷新），则刷新
            if (System.currentTimeMillis() - lastUpdateTime > REFRESH_INTERVAL * 1000) {
                needRefresh = true;
            }
        }

        // 如果不需要刷新，直接返回缓存
        if (!needRefresh && cachedData != null) {
            return cachedData;
        }

        // 2. 需要刷新，尝试获取分布式锁，避免缓存击穿
        String lockKey = RedisConstant.REDIS_LOCK_PREFIX + market + code;
        // 使用setnx来实现分布式锁
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(locked)) {
            try {
                // 获取锁成功，执行远程调用
                log.info("获取锁成功，准备从 Python 服务更新分时数据，市场：{}，股票代码：{}", market, code);
                List<Map<String, Object>> newData = fetchFromPythonService(market, code);

                if (newData != null && !newData.isEmpty()) {
                    // 更新缓存
                    cacheMinuteData(market, code, newData);
                    // 更新刷新时间
                    String updateTimeKey = "stock:minute:updatetime:" + market + code;
                    stringRedisTemplate.opsForValue().set(updateTimeKey, String.valueOf(System.currentTimeMillis()), EXPIRE_TIME, TimeUnit.HOURS);
                    return newData;
                } else {
                    // 获取失败，如果有旧缓存则返回旧缓存，否则返回空
                    return cachedData != null ? cachedData : new ArrayList<>();
                }
            } finally {
                // 释放锁
                stringRedisTemplate.delete(lockKey);
            }
        } else {
            // 获取锁失败，说明有其他线程正在更新
            // 如果有旧数据，直接返回旧数据（降级）
            if (cachedData != null) {
                log.info("获取锁失败，降级返回旧缓存数据，市场：{}，股票代码：{}", market, code);
                return cachedData;
            } else {
                // 如果没有旧数据，稍微等待一下再重试（自旋），或者直接返回空
                try {
                    Thread.sleep(100);
                    return getCachedMinuteData(market, code); // 再次尝试获取
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new ArrayList<>();
                }
            }
        }
    }

    /**
     * 从python服务获取分时数据
     *
     * @param market
     * @param code
     * @return
     */
    private List<Map<String, Object>> fetchFromPythonService(String market, String code) {
        // 从 Python 服务获取分时数据
        String url = UrlConstant.PREDICTION_API_MINUTE_URL.replace("{symbol}", market + code);

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("Python 服务返回非成功状态码: {}", responseEntity.getStatusCode());
                return null;
            }

            String response = responseEntity.getBody();
            if (response == null || response.isEmpty()) {
                return null;
            }

            return JSON.parseObject(response, List.class);
        } catch (Exception e) {
            log.error("调用 Python 服务异常", e);
            return null;
        }
    }

    /**
     * 从Redis中获取当日股票分时信息
     */
    @Override
    public List<Map<String, Object>> getCachedMinuteData(String market, String code) {

        String redisKey = REDIS_KEY_PREFIX + market + code;
        try {
            Object data = stringRedisTemplate.opsForValue().get(redisKey);
            if (data != null) {
                return JSON.parseObject(data.toString(), List.class);
            }
        } catch (Exception e) {
            log.error("从Redis获取分时数据失败，市场：{}，股票代码：{}", market, code, e);
        }
        return null;
    }

    /**
     * 将当日股票分时信息存入Redis，并设置过期时间为1天
     */
    @Override
    public void cacheMinuteData(String market, String code, List<Map<String, Object>> data) {
        String redisKey = REDIS_KEY_PREFIX + market + code;
        // 清空原有列表（如有）
        stringRedisTemplate.delete(redisKey);
        try {
            stringRedisTemplate.opsForValue().set(redisKey, JSON.toJSONString(data), EXPIRE_TIME, TimeUnit.HOURS);
            log.info("分时数据缓存到Redis，市场：{}，股票代码：{}，过期时间：{}小时", market, code, EXPIRE_TIME);
        } catch (Exception e) {
            log.error("分时数据缓存到Redis失败，市场：{}，股票代码：{}", market, code, e);
        }
    }

    // TODO 后续进行微服务拆分后可以考虑用python的akshare获取，用url的话耦合度有点高

    /**
     * 获取股票实时行情（含最新价、涨跌幅等）
     *
     * @param market 市场代码，如 "sh"、"sz"
     * @param code   股票代码，如 "600519"
     * @return 行情 Map，包含最新价等字段，失败返回 null
     */
    public Map<String, Object> getRealTimeQuote(String market, String code) {

        String url = StockConstant.GETREALTIMEQUOTEURL + market + code;
        try {
            String response = HttpUtil.get(url);
            if (response == null || response.isEmpty()) {
                log.warn("实时行情接口返回空，市场：{}，股票代码：{}", market, code);
                return null;
            }

            // 提取引号内的数据
            int start = response.indexOf('"');
            int end = response.lastIndexOf('"');
            if (start == -1 || end == -1 || end <= start) {
                log.warn("实时行情数据格式错误，市场：{}，股票代码：{}", market, code);
                return null;
            }

            String data = response.substring(start + 1, end);
            if (data.isEmpty()) {
                log.warn("实时行情数据内容为空，市场：{}，股票代码：{}", market, code);
                return null;
            }

            // 使用 -1 保留空字段，避免字段丢失
            String[] fields = data.split("~", -1);
            // 确保字段数量足够（至少包含涨跌幅，索引32）
            if (fields.length < 33) {
                log.warn("实时行情数据字段不足，市场：{}，股票代码：{}，实际字段数：{}", market, code, fields.length);
                return null;
            }

            Map<String, Object> quote = new HashMap<>();
            // 安全获取字段（索引以0开始）
            quote.put("code", safeGet(fields, 2, ""));
            quote.put("name", safeGet(fields, 1, ""));
            quote.put("price", toBigDecimal(safeGet(fields, 3, "0")));
            quote.put("prevClose", toBigDecimal(safeGet(fields, 4, "0")));
            quote.put("open", toBigDecimal(safeGet(fields, 5, "0")));
            quote.put("volume", toLong(safeGet(fields, 6, "0")));  // 成交量单位：手，如需股数请 *100
            quote.put("change", toBigDecimal(safeGet(fields, 31, "0")));
            quote.put("changePercent", toBigDecimal(safeGet(fields, 32, "0")));

            return quote;
        } catch (Exception e) {
            log.error("获取实时行情失败，市场：{}，股票代码：{}", market, code, e);
            return null;
        }
    }

    /**
     * 安全获取数组元素，避免越界
     */
    private String safeGet(String[] arr, int index, String defaultValue) {
        if (arr != null && index >= 0 && index < arr.length) {
            String val = arr[index];
            return val == null ? defaultValue : val;
        }
        return defaultValue;
    }

    /**
     * 将字符串转换为BigDecimal，异常时返回默认值0
     */
    private BigDecimal toBigDecimal(String str) {
        if (str == null || str.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(str.trim());
        } catch (NumberFormatException e) {
            log.warn("BigDecimal转换失败，输入值：{}", str);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 将字符串转换为Long，异常时返回默认值0
     */
    private Long toLong(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            log.warn("Long转换失败，输入值：{}", str);
            return 0L;
        }
    }
}
