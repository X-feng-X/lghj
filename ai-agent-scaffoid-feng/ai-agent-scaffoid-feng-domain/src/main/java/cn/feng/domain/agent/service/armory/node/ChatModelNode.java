package cn.feng.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.feng.domain.agent.model.entity.ArmoryCommandEntity;
import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.feng.domain.agent.service.armory.AbstractArmorySupport;
import cn.feng.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.feng.domain.agent.service.armory.matter.mcp.client.TooMcpCreateService;
import cn.feng.domain.agent.service.armory.matter.mcp.client.factory.DefaultMcpClientFactory;
import cn.feng.domain.agent.service.armory.matter.skills.ToolSkillsCreateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatModelNode extends AbstractArmorySupport {

    @Resource
    private AgentNode agentNode;

    @javax.annotation.Resource
    private DefaultMcpClientFactory defaultMcpClientFactory;

    @javax.annotation.Resource
    private ToolSkillsCreateService toolSkillsCreateService;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - ChatModelNode");

        OpenAiApi openAiApi = dynamicContext.getOpenAiApi();

        AiAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        AiAgentConfigTableVO.Module.ChatModel chatModelConfig = aiAgentConfigTableVO.getModule().getChatModel();
        List<AiAgentConfigTableVO.Module.ChatModel.ToolMcp> toolMcpList = chatModelConfig.getToolMcpList();
        List<AiAgentConfigTableVO.Module.ChatModel.ToolSkills> toolSkillsList = chatModelConfig.getToolSkillsList();

        // 构建mcp服务（工厂）
        List<ToolCallback> toolCallbackList = new ArrayList<>();
        if (null != toolMcpList && !toolMcpList.isEmpty()) {
            for (AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp : toolMcpList) {
                TooMcpCreateService tooMcpCreateService = defaultMcpClientFactory.getTooMcpCreateService(toolMcp);
                ToolCallback[] toolCallbacks = tooMcpCreateService.buildToolCallback(toolMcp);
                toolCallbackList.addAll(List.of(toolCallbacks));
            }
        }

        // 构建skills服务
        if (null != toolSkillsList && !toolSkillsList.isEmpty()) {
            for (AiAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills : toolSkillsList) {
                ToolCallback[] toolCallbacks = toolSkillsCreateService.buildToolCallback(toolSkills);
                toolCallbackList.addAll(List.of(toolCallbacks));
            }
        }

        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(chatModelConfig.getModel())
                        .toolCallbacks(toolCallbackList)
                        .build())
                .build();

        dynamicContext.setChatModel(chatModel);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return agentNode;
    }

//    private McpSyncClient createMcpSyncClient(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) throws Exception {
//
//        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.SSEServerParameters sseConfig = toolMcp.getSse();
//        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.StdioServerParameters stdioConfig = toolMcp.getStdio();
//
//        // 处理sse地址
//        if (null != sseConfig) {
//            // http://appbuilder.baidu.com/v2/ai_search/mcp/sse?api_key=bce-v3/ALTAK-JFZXXLpfxhAutDQvJ32Ei/4492c1879b8c2f0df4612ef5b4a52df1c1fba9f7
//
//            // 获取配置的原始基础地址
//            String originalBaseUri = sseConfig.getBaseUri();
//            String baseUri = originalBaseUri;
//            // 获取配置的SSE接口路径
//            String sseEndpoint = sseConfig.getSseEndpoint();
//
//            // 如果SSE接口路径为空，则从原始baseUri中自动解析出接口路径
//            if (StringUtils.isBlank(sseEndpoint)) {
//                URL url = new URL(originalBaseUri);
//
//                // 解析出协议、主机名、端口号
//                String protocol = url.getProtocol(); // 拿到协议
//                String host = url.getHost(); // 拿到主机名
//                int port = url.getPort(); // 拿到端口号
//
//                // 拼接基础URL（不带接口路径）：协议://主机 或 协议://主机:端口
//                String baseUrl = port == -1 ? protocol + "://" + host : protocol + "://" + host + ":" + port;
//
//                // 查找基础URL在原始地址中的位置
//                int index = originalBaseUri.indexOf(baseUrl);
//                if (index != -1) {
//                    // 截取基础URL之后的部分作为SSE接口路径
//                    sseEndpoint = originalBaseUri.substring(index + baseUrl.length());
//                }
//
//                // 将最终使用的baseUri更新为纯基础地址（不含接口）
//                baseUri = baseUrl;
//            }
//
//            // 如果最终SSE接口路径仍为空，设置默认值 /sse
//            sseEndpoint = StringUtils.isBlank(sseEndpoint) ? "/sse" : sseEndpoint;
//
//            // 连接mcp
//            HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport
//                    .builder(baseUri)
//                    .sseEndpoint(sseEndpoint)
//                    .build();
//
//            McpSyncClient mcpSyncClient = McpClient
//                    .sync(sseClientTransport)
//                    .requestTimeout(Duration.ofMillis(sseConfig.getRequestTimeout())).build();
//            // 实例化
//            McpSchema.InitializeResult initialize = mcpSyncClient.initialize();
//
//            log.info("tool sse mcp initialize {}", initialize);
//
//            return mcpSyncClient;
//        }
//
//        // 判断 stdio 配置是否存在（非空则执行本地 MCP 服务初始化）
//        if (null != stdioConfig) {
//            // 从配置中获取 stdio 服务启动参数（命令、参数、环境变量）
//            AiAgentConfigTableVO.Module.ChatModel.ToolMcp.StdioServerParameters.ServerParameters serverParameters = stdioConfig.getServerParameters();
//
//            // 构建 MCP 标准输入输出（stdio）连接所需的参数
//            ServerParameters stdioParams = ServerParameters.builder(serverParameters.getCommand())
//                    .args(serverParameters.getArgs())       // 设置启动命令参数
//                    .env(serverParameters.getEnv())         // 设置环境变量
//                    .build();
//
//            // 创建 MCP 同步客户端：使用 stdio 本地进程通信（无网络端口），指定 JSON 序列化工具
//            McpSyncClient mcpSyncClient = McpClient
//                    .sync(new StdioClientTransport(stdioParams, new JacksonMcpJsonMapper(new ObjectMapper()))) // 创建一个同步的、本地的、用stdio通信的、用json格式的MCP客户端
//                    .requestTimeout(Duration.ofSeconds(stdioConfig.getRequestTimeout())) // 设置请求超时时间
//                    .build();
//
//            // 向本地 MCP 服务发送初始化请求，建立连接
//            McpSchema.InitializeResult initialize = mcpSyncClient.initialize();
//
//            // 打印日志：记录 stdio 模式 MCP 服务初始化结果
//            log.info("tool stdio mcp initialize {}", initialize);
//        }
//
//        throw new RuntimeException("tool mcp sse and stdio is null!");
//    }
}
