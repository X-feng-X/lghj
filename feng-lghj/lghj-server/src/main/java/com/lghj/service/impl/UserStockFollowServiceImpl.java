package com.lghj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.constant.RedisConstant;
import com.lghj.mapper.StockBasicMapper;
import com.lghj.mapper.UserStockFollowMapper;
import com.lghj.pojo.entity.StockBasic;
import com.lghj.pojo.entity.UserStockFollow;
import com.lghj.pojo.vo.StockFollowVO;
import com.lghj.service.IRealTimeStockService;
import com.lghj.service.IUserStockFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserStockFollowServiceImpl extends ServiceImpl<UserStockFollowMapper, UserStockFollow> implements IUserStockFollowService {

    private final StockBasicMapper stockBasicMapper;
    private final IRealTimeStockService realTimeStockService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 添加自选股
     *
     * @param userId 用户ID
     * @param symbol 股票代码
     * @return 成功返回true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFollow(Long userId, String symbol) {
        // 1. 根据 symbol 查询 stock_id
        StockBasic stock = stockBasicMapper.selectOne(new LambdaQueryWrapper<StockBasic>()
                .eq(StockBasic::getSymbol, symbol));
        if (stock == null) {
            log.warn("添加自选股失败，股票不存在: {}", symbol);
            return false;
        }

        // 2. 检查是否已关注（先查Redis）
        String redisKey = RedisConstant.REDIS_FOLLOW_PREFIX + userId;
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(redisKey, symbol))) {
            return true;
        }

        // 查数据库兜底
        Integer count = baseMapper.selectCount(new LambdaQueryWrapper<UserStockFollow>()
                .eq(UserStockFollow::getUserId, userId)
                .eq(UserStockFollow::getStockId, stock.getId()));
        if (count > 0) {
            // 同步回Redis
            stringRedisTemplate.opsForSet().add(redisKey, symbol);
            return true;
        }

        // 3. 插入记录
        UserStockFollow follow = UserStockFollow.builder()
                .userId(userId)
                .stockId(stock.getId())
                .symbol(stock.getSymbol())
                .build();
        boolean result = save(follow);

        // 4. 更新Redis
        if (result) {
            stringRedisTemplate.opsForSet().add(redisKey, symbol);
        }
        return result;
    }

    /**
     * 取消关注
     *
     * @param userId 用户ID
     * @param symbol 股票代码
     * @return 成功返回true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFollow(Long userId, String symbol) {
        // 1. 根据 symbol 查询 stock_id
        StockBasic stock = stockBasicMapper.selectOne(new LambdaQueryWrapper<StockBasic>()
                .eq(StockBasic::getSymbol, symbol));
        if (stock == null) {
            return false;
        }

        // 2. 删除数据库记录
        boolean result = remove(new LambdaQueryWrapper<UserStockFollow>()
                .eq(UserStockFollow::getUserId, userId)
                .eq(UserStockFollow::getStockId, stock.getId()));

        // 3. 删除Redis缓存
        if (result) {
            stringRedisTemplate.opsForSet().remove(RedisConstant.REDIS_FOLLOW_PREFIX + userId, symbol);
        }
        return result;
    }

    /**
     * 查询自选股列表（带行情）
     *
     * @param userId 用户ID
     * @return 自选股VO列表
     */
    @Override
    public List<StockFollowVO> getUserFollowList(Long userId) {

        List<String> symbols;
        String redisKey = RedisConstant.REDIS_FOLLOW_PREFIX + userId;

        // 1. 尝试从Redis获取
        Set<String> redisSymbols = stringRedisTemplate.opsForSet().members(redisKey);
        if (redisSymbols != null && !redisSymbols.isEmpty()) {
            symbols = new ArrayList<>(redisSymbols);
        } else {
            // 2. 缓存不存在，查数据库
            // 查询用户关注的所有 symbol
            List<UserStockFollow> follows = baseMapper.selectList(new LambdaQueryWrapper<UserStockFollow>()
                    .eq(UserStockFollow::getUserId, userId));

            if (follows == null || follows.isEmpty()) {
                return new ArrayList<>();
            }

            symbols = follows.stream().map(UserStockFollow::getSymbol).collect(Collectors.toList());

            // 3. 回写Redis
            if (!symbols.isEmpty()) {
                stringRedisTemplate.opsForSet().add(redisKey, symbols.toArray(new String[0]));
            }
        }

        // 4. 批量获取股票基础信息 (为了拿名称和市场类型)
        // 这里我们可以优化：如果是高频查询，可以将 name 和 marketType 也缓存，或者前端只传 symbol 后端只返回行情
        // 但目前为了完整性，还是查一下 stock_basic
        List<StockBasic> stocks = stockBasicMapper.selectList(new LambdaQueryWrapper<StockBasic>()
                .in(StockBasic::getSymbol, symbols));
        Map<String, StockBasic> stockMap = stocks.stream()
                .collect(Collectors.toMap(StockBasic::getSymbol, s -> s));

        // 5. 构造 VO 并填充实时行情
        return symbols.stream().map(symbol -> {
            StockFollowVO vo = new StockFollowVO();
            vo.setSymbol(symbol);

            StockBasic basic = stockMap.get(symbol);
            if (basic != null) {
                vo.setStockId(basic.getId());
                vo.setName(basic.getName());
                // 获取行情
                String market = getMarketPrefix(basic.getMarketType());
                Map<String, Object> quote = realTimeStockService.getRealTimeQuote(market, symbol);
                if (quote != null) {
                    vo.setPrice((BigDecimal) quote.get("price"));
                    vo.setChangePercent((BigDecimal) quote.get("changePercent"));
                    vo.setVolume((Long) quote.get("volume"));
                }
            } else {
                // 数据库查不到基础信息的情况（理论不应发生）
                vo.setStockId(0L);
                vo.setName(symbol);
            }

            // 处理空值
            if (vo.getPrice() == null) vo.setPrice(BigDecimal.ZERO);
            if (vo.getChangePercent() == null) vo.setChangePercent(BigDecimal.ZERO);
            if (vo.getVolume() == null) vo.setVolume(0L);

            return vo;
        }).collect(Collectors.toList());
    }

    // 辅助方法：根据 marketType 获取前缀
    private String getMarketPrefix(Integer marketType) {
        // 简单映射：1-沪A(sh), 2-深A(sz), 3-创业板(sz), 4-科创板(sh)
        if (marketType == null) return "sh"; // 默认
        if (marketType == 1 || marketType == 4) return "sh";
        return "sz";
    }
}
