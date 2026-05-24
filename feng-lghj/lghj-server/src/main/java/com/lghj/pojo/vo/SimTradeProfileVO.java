package com.lghj.pojo.vo;

import com.lghj.pojo.entity.SimAccount;
import com.lghj.pojo.entity.TradeDeal;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.pojo.entity.UserPosition;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class SimTradeProfileVO {

    private Long userId;
    private SimAccount account;
    private List<UserPosition> positions;
    private List<TradeOrder> recentOrders;
    private List<TradeDeal> recentDeals;
    private Summary summary;

    @Data
    @Builder
    public static class Summary {
        private int positionCount;
        private int orderCount;
        private int dealCount;
        private int buyOrderCount;
        private int sellOrderCount;
        private int canceledOrderCount;
        private int completedOrderCount;
        private BigDecimal buyAmount;
        private BigDecimal sellAmount;
        private BigDecimal currentPositionCost;
        private BigDecimal realizedTurnover;
        private String topPositionSymbol;
        private BigDecimal topPositionCostRatio;
        private List<String> activeSymbols;
        private Map<String, Integer> dealCountBySymbol;
        private List<String> behaviorTags;
    }
}
