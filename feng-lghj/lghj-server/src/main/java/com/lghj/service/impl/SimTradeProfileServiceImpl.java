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

@Service
@RequiredArgsConstructor
public class SimTradeProfileServiceImpl implements ISimTradeProfileService {

    private static final int RECENT_LIMIT = 30;

    private final IAccountService accountService;
    private final ITradeService tradeService;

    @Override
    public SimTradeProfileVO queryProfile(Long userId) {
        SimAccount account = accountService.getAccountByUserId(userId);
        List<UserPosition> positions = accountService.getUserPositions(userId);
        List<TradeOrder> orders = tradeService.getUserOrders(userId);
        List<TradeDeal> deals = tradeService.getUserDeals(userId);

        return SimTradeProfileVO.builder()
                .userId(userId)
                .account(account)
                .positions(limit(positions))
                .recentOrders(limit(orders))
                .recentDeals(limit(deals))
                .summary(buildSummary(positions, orders, deals))
                .build();
    }

    private SimTradeProfileVO.Summary buildSummary(List<UserPosition> positions,
                                                   List<TradeOrder> orders,
                                                   List<TradeDeal> deals) {
        BigDecimal buyAmount = dealAmount(deals, (short) 1);
        BigDecimal sellAmount = dealAmount(deals, (short) 2);
        BigDecimal currentPositionCost = positions.stream()
                .map(this::positionCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        UserPosition topPosition = positions.stream()
                .max(Comparator.comparing(this::positionCost))
                .orElse(null);

        BigDecimal topPositionCostRatio = BigDecimal.ZERO;
        if (topPosition != null && currentPositionCost.compareTo(BigDecimal.ZERO) > 0) {
            topPositionCostRatio = positionCost(topPosition)
                    .divide(currentPositionCost, 4, RoundingMode.HALF_UP);
        }

        Map<String, Integer> dealCountBySymbol = deals.stream()
                .filter(deal -> deal.getSymbol() != null)
                .collect(Collectors.groupingBy(TradeDeal::getSymbol, LinkedHashMap::new, Collectors.summingInt(deal -> 1)));

        return SimTradeProfileVO.Summary.builder()
                .positionCount(positions.size())
                .orderCount(orders.size())
                .dealCount(deals.size())
                .buyOrderCount(countOrders(orders, (short) 1))
                .sellOrderCount(countOrders(orders, (short) 2))
                .canceledOrderCount(countStatus(orders, (short) 4))
                .completedOrderCount(countStatus(orders, (short) 3))
                .buyAmount(buyAmount)
                .sellAmount(sellAmount)
                .currentPositionCost(currentPositionCost)
                .realizedTurnover(buyAmount.add(sellAmount))
                .topPositionSymbol(topPosition == null ? null : topPosition.getSymbol())
                .topPositionCostRatio(topPositionCostRatio)
                .activeSymbols(activeSymbols(positions, deals))
                .dealCountBySymbol(dealCountBySymbol)
                .behaviorTags(behaviorTags(positions, orders, deals, topPositionCostRatio))
                .build();
    }

    private int countOrders(List<TradeOrder> orders, Short direction) {
        return (int) orders.stream()
                .filter(order -> Objects.equals(order.getDirection(), direction))
                .count();
    }

    private int countStatus(List<TradeOrder> orders, Short status) {
        return (int) orders.stream()
                .filter(order -> Objects.equals(order.getStatus(), status))
                .count();
    }

    private BigDecimal dealAmount(List<TradeDeal> deals, Short direction) {
        return deals.stream()
                .filter(deal -> Objects.equals(deal.getDealDirection(), direction))
                .map(deal -> nullToZero(deal.getPrice()).multiply(BigDecimal.valueOf(nullToZero(deal.getQuantity()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal positionCost(UserPosition position) {
        return nullToZero(position.getCostPrice()).multiply(BigDecimal.valueOf(nullToZero(position.getTotalQuantity())));
    }

    private List<String> activeSymbols(List<UserPosition> positions, List<TradeDeal> deals) {
        Map<String, Boolean> symbols = new LinkedHashMap<>();
        positions.stream().map(UserPosition::getSymbol).filter(Objects::nonNull).forEach(symbol -> symbols.put(symbol, true));
        deals.stream().map(TradeDeal::getSymbol).filter(Objects::nonNull).forEach(symbol -> symbols.put(symbol, true));
        return new ArrayList<>(symbols.keySet());
    }

    private List<String> behaviorTags(List<UserPosition> positions,
                                      List<TradeOrder> orders,
                                      List<TradeDeal> deals,
                                      BigDecimal topPositionCostRatio) {
        List<String> tags = new ArrayList<>();
        if (deals.isEmpty() && orders.isEmpty()) {
            tags.add("NO_TRADE_RECORD");
        }
        if (positions.size() == 1 || topPositionCostRatio.compareTo(new BigDecimal("0.60")) >= 0) {
            tags.add("CONCENTRATED_POSITION");
        }
        if (orders.size() >= 20) {
            tags.add("HIGH_ORDER_FREQUENCY");
        }
        if (countStatus(orders, (short) 4) >= 5) {
            tags.add("FREQUENT_CANCEL");
        }
        if (countOrders(orders, (short) 1) > countOrders(orders, (short) 2) * 2 && !orders.isEmpty()) {
            tags.add("BUY_SIDE_BIAS");
        }
        if (deals.stream().map(TradeDeal::getSymbol).filter(Objects::nonNull).distinct().count() >= 5) {
            tags.add("DIVERSIFIED_TRADING");
        }
        return tags;
    }

    private <T> List<T> limit(List<T> records) {
        if (records.size() <= RECENT_LIMIT) {
            return records;
        }
        return records.subList(0, RECENT_LIMIT);
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
