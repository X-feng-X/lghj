package cn.feng.config;

import cn.feng.tool.InvestmentTradeProfileTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvestmentToolConfig {

    @Bean("investmentTradeToolCallbackProvider")
    public ToolCallbackProvider investmentTradeToolCallbackProvider(InvestmentTradeProfileTool investmentTradeProfileTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(investmentTradeProfileTool)
                .build();
    }
}
