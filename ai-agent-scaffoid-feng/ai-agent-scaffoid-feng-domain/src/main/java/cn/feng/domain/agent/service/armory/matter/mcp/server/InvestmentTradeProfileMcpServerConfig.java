package cn.feng.domain.agent.service.armory.matter.mcp.server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvestmentTradeProfileMcpServerConfig {

    @Bean("investmentTradeProfileMcp")
    public ToolCallbackProvider investmentTradeProfileMcp(InvestmentTradeProfileMcpService investmentTradeProfileMcpService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(investmentTradeProfileMcpService)
                .build();
    }
}
