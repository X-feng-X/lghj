package com.lghj.service.impl;

import com.lghj.pojo.entity.SimAccount;
import com.lghj.pojo.entity.TradeDeal;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.pojo.entity.UserPosition;
import com.lghj.pojo.vo.SimTradeProfileVO;
import com.lghj.service.IAccountService;
import com.lghj.service.ISimTradeProfileService;
import com.lghj.service.ITradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 模拟交易用户画像服务实现类
 * 核心功能：根据用户ID，聚合查询用户的账户、持仓、订单、成交数据
 * 并生成交易统计报表 + 行为标签，返回给前端展示个人交易画像
 *
 * @author lghj
 */
@Service
@RequiredArgsConstructor // 构造器注入依赖，替代@Autowired
public class SimTradeProfileServiceImpl implements ISimTradeProfileService {

    /**
     * 最近记录最大限制数：订单/持仓/成交只返回最近30条，避免数据量过大
     */
    private static final int RECENT_LIMIT = 30;

    // ====================== 依赖注入 ======================
    /**
     * 账户相关服务：查询账户、持仓
     */
    private final IAccountService accountService;
    /**
     * 交易相关服务：查询订单、成交
     */
    private final ITradeService tradeService;

    // ====================== 核心接口方法 ======================

    /**
     * 查询用户模拟交易画像（对外提供的核心接口）
     *
     * @param userId 用户ID
     * @return 封装好的交易画像VO（包含账户、持仓、订单、成交、统计、标签）
     */
    @Override
    public SimTradeProfileVO queryProfile(Long userId) {
        // 1. 查询用户基础数据
        SimAccount account = accountService.getAccountByUserId(userId);        // 模拟账户信息
        List<UserPosition> positions = accountService.getUserPositions(userId); // 用户持仓列表
        List<TradeOrder> orders = tradeService.getUserOrders(userId);          // 用户所有订单
        List<TradeDeal> deals = tradeService.getUserDeals(userId);            // 用户所有成交

        // 2. 构建并返回画像VO（只返回最近30条记录，统计数据完整计算）
        return SimTradeProfileVO.builder()
                .userId(userId)
                .account(account)                               // 账户信息
                .positions(limit(positions))                    // 持仓（限制30条）
                .recentOrders(limit(orders))                    // 最近订单（限制30条）
                .recentDeals(limit(deals))                      // 最近成交（限制30条）
                .summary(buildSummary(positions, orders, deals))// 交易统计汇总（核心）
                .build();
    }

    // ====================== 统计汇总构建（核心方法） ======================

    /**
     * 构建交易统计汇总信息
     * 计算：订单数、成交数、买卖金额、重仓信息、交易品种、行为标签等
     *
     * @param positions 持仓列表
     * @param orders    订单列表
     * @param deals     成交列表
     * @return 统计汇总对象
     */
    private SimTradeProfileVO.Summary buildSummary(List<UserPosition> positions,
                                                   List<TradeOrder> orders,
                                                   List<TradeDeal> deals) {
        // 1. 计算买卖总成交额
        BigDecimal buyAmount = dealAmount(deals, (short) 1);      // 买入总金额
        BigDecimal sellAmount = dealAmount(deals, (short) 2);     // 卖出总金额

        // 2. 计算当前所有持仓的总成本
        BigDecimal currentPositionCost = positions.stream()
                .map(this::positionCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 找出持仓成本最高的标的（第一重仓）
        UserPosition topPosition = positions.stream()
                .max(Comparator.comparing(this::positionCost))
                .orElse(null);

        // 4. 计算重仓股占总持仓成本的比例（保留4位小数）
        BigDecimal topPositionCostRatio = BigDecimal.ZERO;
        if (topPosition != null && currentPositionCost.compareTo(BigDecimal.ZERO) > 0) {
            topPositionCostRatio = positionCost(topPosition)
                    .divide(currentPositionCost, 4, RoundingMode.HALF_UP);
        }

        // 5. 按交易代码分组统计：每个标的成交次数（保持插入顺序）
        Map<String, Integer> dealCountBySymbol = deals.stream()
                .filter(deal -> deal.getSymbol() != null)
                .collect(Collectors.groupingBy(
                        TradeDeal::getSymbol,
                        LinkedHashMap::new,
                        Collectors.summingInt(deal -> 1)
                ));

        // 6. 封装所有统计数据返回
        return SimTradeProfileVO.Summary.builder()
                .positionCount(positions.size())                  // 持仓数量
                .orderCount(orders.size())                       // 总订单数
                .dealCount(deals.size())                         // 总成交数
                .buyOrderCount(countOrders(orders, (short) 1))   // 买入订单数
                .sellOrderCount(countOrders(orders, (short) 2))  // 卖出订单数
                .canceledOrderCount(countStatus(orders, (short) 4)) // 已撤销订单数
                .completedOrderCount(countStatus(orders, (short) 3)) // 已完成订单数
                .buyAmount(buyAmount)                            // 买入总金额
                .sellAmount(sellAmount)                          // 卖出总金额
                .currentPositionCost(currentPositionCost)        // 当前持仓总成本
                .realizedTurnover(buyAmount.add(sellAmount))     // 累计成交额
                .topPositionSymbol(topPosition == null ? null : topPosition.getSymbol()) // 重仓代码
                .topPositionCostRatio(topPositionCostRatio)      // 重仓占比
                .activeSymbols(activeSymbols(positions, deals))  // 活跃交易品种
                .dealCountBySymbol(dealCountBySymbol)            // 各品种成交次数
                .behaviorTags(behaviorTags(positions, orders, deals, topPositionCostRatio)) // 行为标签
                .build();
    }

    // ====================== 工具方法：统计计数 ======================

    /**
     * 统计指定方向的订单数量（1=买入，2=卖出）
     */
    private int countOrders(List<TradeOrder> orders, Short direction) {
        return (int) orders.stream()
                .filter(order -> Objects.equals(order.getDirection(), direction))
                .count();
    }

    /**
     * 统计指定状态的订单数量（3=完成，4=撤销）
     */
    private int countStatus(List<TradeOrder> orders, Short status) {
        return (int) orders.stream()
                .filter(order -> Objects.equals(order.getStatus(), status))
                .count();
    }

    // ====================== 工具方法：金额计算 ======================

    /**
     * 计算指定方向的成交总金额 = 成交价 × 成交数量
     * @param direction 1=买入，2=卖出
     */
    private BigDecimal dealAmount(List<TradeDeal> deals, Short direction) {
        return deals.stream()
                .filter(deal -> Objects.equals(deal.getDealDirection(), direction))
                .map(deal -> nullToZero(deal.getPrice()).multiply(BigDecimal.valueOf(nullToZero(deal.getQuantity()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算单个持仓的成本 = 成本价 × 总持仓数量
     */
    private BigDecimal positionCost(UserPosition position) {
        return nullToZero(position.getCostPrice()).multiply(BigDecimal.valueOf(nullToZero(position.getTotalQuantity())));
    }

    // ====================== 工具方法：交易品种 ======================

    /**
     * 获取用户所有活跃交易品种（持仓有过 + 成交过）
     * 去重并保持顺序
     */
    private List<String> activeSymbols(List<UserPosition> positions, List<TradeDeal> deals) {
        Map<String, Boolean> symbols = new LinkedHashMap<>();
        // 加入持仓品种
        positions.stream().map(UserPosition::getSymbol).filter(Objects::nonNull).forEach(symbol -> symbols.put(symbol, true));
        // 加入成交品种
        deals.stream().map(TradeDeal::getSymbol).filter(Objects::nonNull).forEach(symbol -> symbols.put(symbol, true));
        return new ArrayList<>(symbols.keySet());
    }

    // ====================== 工具方法：行为标签 ======================

    /**
     * 生成用户交易行为标签（自动识别用户交易风格）
     * 例如：无交易、重仓、高频、频繁撤单、偏爱买入、分散交易
     */
    private List<String> behaviorTags(List<UserPosition> positions,
                                      List<TradeOrder> orders,
                                      List<TradeDeal> deals,
                                      BigDecimal topPositionCostRatio) {
        List<String> tags = new ArrayList<>();

        // 无任何交易记录
        if (deals.isEmpty() && orders.isEmpty()) {
            tags.add("NO_TRADE_RECORD");
        }
        // 集中持仓（只有1个持仓 或 单票持仓>60%）
        if (positions.size() == 1 || topPositionCostRatio.compareTo(new BigDecimal("0.60")) >= 0) {
            tags.add("CONCENTRATED_POSITION");
        }
        // 下单频率高（订单≥20）
        if (orders.size() >= 20) {
            tags.add("HIGH_ORDER_FREQUENCY");
        }
        // 频繁撤单（撤单≥5）
        if (countStatus(orders, (short) 4) >= 5) {
            tags.add("FREQUENT_CANCEL");
        }
        // 偏爱买入（买入订单是卖出的2倍以上）
        if (countOrders(orders, (short) 1) > countOrders(orders, (short) 2) * 2 && !orders.isEmpty()) {
            tags.add("BUY_SIDE_BIAS");
        }
        // 分散交易（交易品种≥5）
        if (deals.stream().map(TradeDeal::getSymbol).filter(Objects::nonNull).distinct().count() >= 5) {
            tags.add("DIVERSIFIED_TRADING");
        }

        return tags;
    }

    // ====================== 通用工具方法 ======================

    /**
     * 限制列表返回数量：只返回前N条（最近30条）
     */
    private <T> List<T> limit(List<T> records) {
        if (records.size() <= RECENT_LIMIT) {
            return records;
        }
        return records.subList(0, RECENT_LIMIT);
    }

    /**
     * 空值安全：BigDecimal为null时返回0
     */
    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 空值安全：Integer为null时返回0
     */
    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}