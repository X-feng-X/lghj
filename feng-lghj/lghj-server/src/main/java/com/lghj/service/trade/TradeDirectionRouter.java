package com.lghj.service.trade;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TradeDirectionRouter {

    private final Map<Short, TradeDirectionStrategy> strategyMap;

    public TradeDirectionRouter(List<TradeDirectionStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(TradeDirectionStrategy::direction, Function.identity()));
    }

    public TradeDirectionStrategy route(short direction) {
        TradeDirectionStrategy strategy = strategyMap.get(direction);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported trade direction: " + direction);
        }
        return strategy;
    }
}
