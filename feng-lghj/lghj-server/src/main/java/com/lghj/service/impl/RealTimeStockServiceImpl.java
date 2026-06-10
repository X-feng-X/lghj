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
import java.util.Locale;
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
        String keyword = normalizeNewsKeyword(symbol);

        // TODO 可以考虑存Redis
        // 构造内部参数
        Map<String, Object> innerParam = new HashMap<>();
        innerParam.put("uid", "");
        innerParam.put("keyword", keyword);
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
                    .header("Referer", "https://so.eastmoney.com/news/s?keyword=" + keyword)
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
                            vo.setKeyword(keyword);
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
            log.error("获取股票新闻异常，symbol={}, keyword={}", symbol, keyword, e);
        }
        return new ArrayList<>();
    }

    private String normalizeNewsKeyword(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return "股市";
        }

        String normalized = symbol.trim().toLowerCase(Locale.ROOT);
        if ("sh000001".equals(normalized) || "000001.sh".equals(normalized)) {
            return "上证指数";
        }
        if ("sz399001".equals(normalized) || "399001.sz".equals(normalized)) {
            return "深证成指";
        }
        if ("sz399006".equals(normalized) || "399006.sz".equals(normalized)) {
            return "创业板指";
        }
        if (normalized.startsWith("sh") || normalized.startsWith("sz")) {
            return normalized.substring(2);
        }
        return symbol.trim();
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

    @Override
    public List<Map<String, Object>> getStockHistory(String symbol, String period) {
        StockSymbol stockSymbol = normalizeStockSymbol(symbol);
        if (stockSymbol == null) {
            log.warn("历史K线股票代码为空或非法，symbol={}", symbol);
            return new ArrayList<>();
        }

        String normalizedPeriod = normalizePeriod(period);
        String redisKey = RedisConstant.STOCK_HISTORY_KEY + stockSymbol.market + stockSymbol.code + ":" + normalizedPeriod;

        try {
            String cached = stringRedisTemplate.opsForValue().get(redisKey);
            if (cached != null && !cached.isEmpty()) {
                return JSON.parseObject(cached, List.class);
            }
        } catch (Exception e) {
            log.warn("读取历史K线缓存失败，key={}", redisKey, e);
            stringRedisTemplate.delete(redisKey);
        }

        List<Map<String, Object>> history = fetchHistoryFromSina(stockSymbol, normalizedPeriod);
        if (history == null || history.isEmpty()) {
            history = fetchHistoryFromTencent(stockSymbol, normalizedPeriod);
        }
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            stringRedisTemplate.opsForValue().set(redisKey, JSON.toJSONString(history), EXPIRE_TIME, TimeUnit.HOURS);
            log.info("历史K线数据缓存到Redis，key={}, 过期时间={}小时", redisKey, EXPIRE_TIME);
        } catch (Exception e) {
            log.warn("写入历史K线缓存失败，key={}", redisKey, e);
        }

        return history;
    }

    private List<Map<String, Object>> fetchHistoryFromSina(StockSymbol stockSymbol, String period) {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("symbol", stockSymbol.market + stockSymbol.code);
        requestParams.put("scale", toSinaScale(period));
        requestParams.put("ma", "no");
        requestParams.put("datalen", 1000);

        try {
            String response = HttpUtil.createGet(UrlConstant.STOCK_HISTORY_SINA_KLINE_URL)
                    .form(requestParams)
                    .timeout(5000)
                    .header("Referer", "http://finance.sina.com.cn/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
                    .execute()
                    .body();

            if (response == null || response.isEmpty()) {
                log.warn("新浪历史K线接口返回空，市场：{}，股票代码：{}，周期：{}", stockSymbol.market, stockSymbol.code, period);
                return new ArrayList<>();
            }

            String jsonArrayText = extractJsonArray(response);
            if (jsonArrayText == null) {
                log.warn("新浪历史K线接口返回格式异常，市场：{}，股票代码：{}，周期：{}", stockSymbol.market, stockSymbol.code, period);
                return new ArrayList<>();
            }

            JSONArray klines = JSON.parseArray(jsonArrayText);
            if (klines == null || klines.isEmpty()) {
                log.warn("新浪历史K线接口无数据，市场：{}，股票代码：{}，周期：{}", stockSymbol.market, stockSymbol.code, period);
                return new ArrayList<>();
            }

            List<Map<String, Object>> history = new ArrayList<>();
            for (int i = 0; i < klines.size(); i++) {
                JSONObject line = klines.getJSONObject(i);
                if (line == null) {
                    continue;
                }

                String date = line.getString("day");
                Map<String, Object> item = new HashMap<>();
                item.put("date", date != null && date.length() >= 10 ? date.substring(0, 10) : date);
                item.put("open", toBigDecimal(line.getString("open")));
                item.put("close", toBigDecimal(line.getString("close")));
                item.put("high", toBigDecimal(line.getString("high")));
                item.put("low", toBigDecimal(line.getString("low")));
                item.put("volume", toLong(line.getString("volume")));
                history.add(item);
            }

            return history;
        } catch (Exception e) {
            log.warn("获取新浪历史K线失败，市场：{}，股票代码：{}，周期：{}，原因：{}", stockSymbol.market, stockSymbol.code, period, e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> fetchHistoryFromTencent(StockSymbol stockSymbol, String period) {
        Map<String, Object> requestParams = new HashMap<>();
        String symbol = stockSymbol.market + stockSymbol.code;
        String klineType = toTencentKlineType(period);
        requestParams.put("param", symbol + "," + klineType + ",,,1000");

        try {
            String response = HttpUtil.createGet(UrlConstant.STOCK_HISTORY_TENCENT_KLINE_URL)
                    .form(requestParams)
                    .timeout(5000)
                    .header("Referer", "https://gu.qq.com/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
                    .execute()
                    .body();

            if (response == null || response.isEmpty()) {
                log.warn("历史K线接口返回空，市场：{}，股票代码：{}", stockSymbol.market, stockSymbol.code);
                return new ArrayList<>();
            }

            JSONObject jsonObject = JSON.parseObject(response);
            JSONObject data = jsonObject == null ? null : jsonObject.getJSONObject("data");
            JSONObject stockData = data == null ? null : data.getJSONObject(symbol);
            JSONArray klines = stockData == null ? null : stockData.getJSONArray(klineType);
            if (klines == null || klines.isEmpty()) {
                log.warn("历史K线接口无数据，市场：{}，股票代码：{}，周期：{}", stockSymbol.market, stockSymbol.code, period);
                return new ArrayList<>();
            }

            List<Map<String, Object>> history = new ArrayList<>();
            for (int i = 0; i < klines.size(); i++) {
                JSONArray line = klines.getJSONArray(i);
                if (line == null || line.size() < 6) {
                    continue;
                }

                Map<String, Object> item = new HashMap<>();
                item.put("date", line.getString(0));
                item.put("open", toBigDecimal(line.getString(1)));
                item.put("close", toBigDecimal(line.getString(2)));
                item.put("high", toBigDecimal(line.getString(3)));
                item.put("low", toBigDecimal(line.getString(4)));
                item.put("volume", toLong(line.getString(5)));
                history.add(item);
            }

            return history;
        } catch (Exception e) {
            log.warn("获取历史K线失败，市场：{}，股票代码：{}，周期：{}，原因：{}", stockSymbol.market, stockSymbol.code, period, e.getMessage());
            return new ArrayList<>();
        }
    }

    private StockSymbol normalizeStockSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return null;
        }

        String normalized = symbol.trim().toLowerCase(Locale.ROOT);
        String market;
        String code;
        if (normalized.startsWith("sh") || normalized.startsWith("sz")) {
            market = normalized.substring(0, 2);
            code = normalized.substring(2);
        } else {
            code = normalized;
            market = code.startsWith("5") || code.startsWith("6") || code.startsWith("9") ? "sh" : "sz";
        }

        if (code.isEmpty()) {
            return null;
        }

        return new StockSymbol(market, code);
    }

    private String normalizePeriod(String period) {
        String normalized = period == null ? "D" : period.trim().toUpperCase(Locale.ROOT);
        if (!"W".equals(normalized) && !"M".equals(normalized)) {
            return "D";
        }
        return normalized;
    }

    private String toTencentKlineType(String period) {
        if ("W".equals(period)) {
            return "week";
        }
        if ("M".equals(period)) {
            return "month";
        }
        return "day";
    }

    private int toSinaScale(String period) {
        if ("W".equals(period)) {
            return 1200;
        }
        if ("M".equals(period)) {
            return 7200;
        }
        return 240;
    }

    private String extractJsonArray(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start < 0 || end <= start) {
            return null;
        }
        return response.substring(start, end + 1);
    }

    private static class StockSymbol {
        private final String market;
        private final String code;

        private StockSymbol(String market, String code) {
            this.market = market;
            this.code = code;
        }
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

        String normalizedMarket = market == null ? "" : market.trim().toLowerCase();
        String normalizedCode = code == null ? "" : code.trim();
        String redisKey = RedisConstant.STOCK_REAL_TIME_KEY + normalizedMarket + normalizedCode;

        try {
            String cached = stringRedisTemplate.opsForValue().get(redisKey);
            if (cached != null && !cached.isEmpty()) {
                return JSON.parseObject(cached, Map.class);
            }
        } catch (Exception e) {
            log.warn("读取实时行情缓存失败，key={}", redisKey, e);
            stringRedisTemplate.delete(redisKey);
        }

        String url = StockConstant.GETREALTIMEQUOTEURL + normalizedMarket + normalizedCode;
        try {
            String response = HttpUtil.get(url);
            if (response == null || response.isEmpty()) {
                log.warn("实时行情接口返回空，市场：{}，股票代码：{}", normalizedMarket, normalizedCode);
                return null;
            }

            // 提取引号内的数据
            int start = response.indexOf('"');
            int end = response.lastIndexOf('"');
            if (start == -1 || end == -1 || end <= start) {
                log.warn("实时行情数据格式错误，市场：{}，股票代码：{}", normalizedMarket, normalizedCode);
                return null;
            }

            String data = response.substring(start + 1, end);
            if (data.isEmpty()) {
                log.warn("实时行情数据内容为空，市场：{}，股票代码：{}", normalizedMarket, normalizedCode);
                return null;
            }

            // 使用 -1 保留空字段，避免字段丢失
            String[] fields = data.split("~", -1);
            // 确保字段数量足够（至少包含涨跌幅，索引32）
            if (fields.length < 33) {
                log.warn("实时行情数据字段不足，市场：{}，股票代码：{}，实际字段数：{}", normalizedMarket, normalizedCode, fields.length);
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

            try {
                stringRedisTemplate.opsForValue().set(redisKey, JSON.toJSONString(quote), EXPIRE_TIME, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("写入实时行情缓存失败，key={}", redisKey, e);
            }

            return quote;
        } catch (Exception e) {
            log.error("获取实时行情失败，市场：{}，股票代码：{}", normalizedMarket, normalizedCode, e);
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
