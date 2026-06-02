package cn.feng.domain.agent.service.armory.matter.mcp.server;

import cn.feng.domain.agent.adapter.port.SimTradeProfilePort;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI 投资顾问 - 模拟交易画像 MCP 工具服务
 * 核心作用：给 Spring AI 大模型提供【查询用户模拟交易画像】的能力
 * 属于 AI Agent 的工具类，模型可自动调用此方法获取用户交易数据
 *
 * @author feng
 */
@Slf4j          // 日志注解
@Service         // Spring 服务注册
@RequiredArgsConstructor // 构造器注入依赖
public class InvestmentTradeProfileMcpService {

    /**
     * 模拟交易画像端口适配器
     * 用于远程调用内部接口，获取用户交易画像JSON数据
     */
    private final SimTradeProfilePort simTradeProfilePort;

    /**
     * AI 大模型可调用的工具方法
     * 注解 @Tool 表示这是一个 AI 工具函数，模型会自动识别并调用
     *
     * @param request 请求参数（包含用户ID）
     * @return 交易画像响应（包含JSON字符串）
     */
    @Tool(description = "根据当前用户ID查询量股化金模拟交易画像，包括账户、持仓、近期委托、近期成交、交易行为标签和仓位集中度。只用于个性化投资顾问分析。")
    public SimTradeProfileResponse querySimTradeProfile(SimTradeProfileRequest request) {
        // 构建返回对象
        SimTradeProfileResponse response = new SimTradeProfileResponse();

        // 1. 参数校验：请求对象为空 或 用户ID为空
        if (request == null || !StringUtils.hasText(request.getUserId())) {
            response.setSuccess(false);
            response.setMessage("缺少当前用户ID，无法查询模拟交易画像。");
            return response;
        }

        // 2. 调用端口层，远程获取用户交易画像的 JSON 字符串
        String profileJson = simTradeProfilePort.queryProfileJson(request.getUserId());

        // 3. 结果校验：未获取到有效画像数据
        if (!StringUtils.hasText(profileJson)) {
            response.setSuccess(false);
            response.setMessage("未获取到模拟交易画像，可能是账户不存在、暂无交易记录或交易系统不可用。");
            return response;
        }

        // 4. 成功：封装数据返回
        response.setSuccess(true);
        response.setMessage("OK");
        response.setProfileJson(profileJson);
        return response;
    }

    /**
     * AI 工具 - 查询模拟交易画像 请求体
     * 用于 AI 模型传入参数（JSON 格式自动映射）
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL) // 不序列化为null的字段
    public static class SimTradeProfileRequest {

        /**
         * 用户ID
         * required = true 表示 AI 模型必须传此参数
         * description 给模型看的说明，指导模型正确传参
         */
        @JsonProperty(required = true, value = "userId")
        @JsonPropertyDescription("当前对话用户ID。必须使用系统上下文中的当前用户ID，不要编造或改查其他用户。")
        private String userId;
    }

    /**
     * AI 工具 - 查询模拟交易画像 响应体
     * 把查询结果返回给 AI 大模型解析
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SimTradeProfileResponse {

        /**
         * 调用是否成功
         */
        @JsonProperty(required = true, value = "success")
        @JsonPropertyDescription("是否成功获取画像")
        private boolean success;

        /**
         * 状态信息/错误提示
         */
        @JsonProperty(value = "message")
        @JsonPropertyDescription("查询状态或错误信息")
        private String message;

        /**
         * 交易画像完整JSON字符串
         * 包含：账户、持仓、订单、成交、统计、行为标签等全套数据
         */
        @JsonProperty(value = "profileJson")
        @JsonPropertyDescription("量股化金返回的模拟交易画像 JSON，包含 data.account、data.positions、data.recentOrders、data.recentDeals、data.summary")
        private String profileJson;
    }
}
