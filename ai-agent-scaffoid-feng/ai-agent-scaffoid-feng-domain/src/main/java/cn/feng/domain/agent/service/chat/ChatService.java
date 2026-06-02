package cn.feng.domain.agent.service.chat;

import cn.feng.domain.agent.adapter.port.SimTradeProfilePort;
import cn.feng.domain.agent.model.entity.ChatCommandEntity;
import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.feng.domain.agent.model.valobj.properties.AiAgentAutoConfigProperties;
import cn.feng.domain.agent.service.IChatService;
import cn.feng.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.feng.types.enums.ResponseCode;
import cn.feng.types.exception.AppException;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 智能体对话服务实现类
 * 核心功能：处理用户与 AI 投顾的聊天对话
 * 包括：创建会话、发送消息、流式/非流式返回、自动注入用户交易画像上下文
 * 专门服务于【投资顾问 AI 智能体】
 *
 * @author feng
 */
@Slf4j
@Service
public class ChatService implements IChatService {

    /**
     * 投资顾问智能体唯一标识（固定ID）
     */
    private static final String INVESTMENT_ADVISOR_AGENT_ID = "investment-advisor";

    /**
     * 交易画像上下文最大长度限制，防止超长导致AI模型处理失败
     */
    private static final int MAX_PROFILE_CONTEXT_LENGTH = 6000;

    /**
     * AI 智能体工厂：用于获取注册好的 AI 智能体、Runner 执行器
     */
    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    /**
     * AI 智能体自动配置：读取配置文件中的智能体配置
     */
    @Resource
    private AiAgentAutoConfigProperties aiAgentAutoConfigProperties;

    /**
     * 模拟交易画像端口：远程调用获取用户交易画像JSON
     */
    @Resource
    private SimTradeProfilePort simTradeProfilePort;

    /**
     * 用户会话缓存：userId -> sessionId
     * 用于保持用户与AI的对话上下文（记忆功能）
     * ConcurrentHashMap 保证线程安全
     */
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    // ====================== 对外接口：查询AI智能体配置列表 ======================

    /**
     * 查询所有可用的 AI 智能体配置列表
     * 用于前端展示可选择的 AI 角色（如：投资顾问、行情助手等）
     */
    @Override
    public List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList() {
        // 获取所有AI智能体配置
        Map<String, AiAgentConfigTableVO> tables = aiAgentAutoConfigProperties.getTables();

        List<AiAgentConfigTableVO.Agent> agentList = new ArrayList<>();
        if (tables != null) {
            // 遍历配置，提取所有 Agent 信息
            for (AiAgentConfigTableVO vo : tables.values()) {
                if (vo.getAgent() != null) {
                    agentList.add(vo.getAgent());
                }
            }
        }

        return agentList;
    }

    // ====================== 对外接口：创建AI对话会话 ======================

    /**
     * 创建用户与 AI 的对话会话（保持上下文记忆）
     * 同一个用户多次对话，复用同一个 sessionId
     *
     * @param agentId AI智能体ID
     * @param userId  用户ID
     * @return 会话ID
     */
    @Override
    public String createSession(String agentId, String userId) {
        // 从工厂获取已注册的 AI 智能体信息
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        // 校验智能体是否存在
        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        // 获取应用名称和执行器
        String appName = aiAgentRegisterVO.getAppName();
        InMemoryRunner runner = aiAgentRegisterVO.getRunner();

        // 缓存会话：同一个用户只创建一次会话，复用会话ID
        return userSessions.computeIfAbsent(userId, uid -> {
            // 调用谷歌AI SDK创建会话
            Session session = runner.sessionService().createSession(appName, uid).blockingGet();
            return session.id();
        });
    }

    // ====================== 对外接口：处理消息（多种重载方法） ======================

    /**
     * 处理用户消息（简易版：自动创建会话）
     *
     * @param agentId 智能体ID
     * @param userId  用户ID
     * @param message 用户消息
     * @return AI 返回的文本列表
     */
    @Override
    public List<String> handleMessage(String agentId, String userId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        // 先创建/获取会话
        String sessionId = createSession(agentId, userId);
        // 调用完整方法处理消息
        return handleMessage(agentId, userId, sessionId, message);
    }

    /**
     * 处理用户消息（标准版：带会话ID）
     * 阻塞式等待AI返回结果
     *
     * @return AI 输出的消息列表
     */
    @Override
    public List<String> handleMessage(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        // 获取执行器
        InMemoryRunner runner = aiAgentRegisterVO.getRunner();
        // 构建用户消息（自动注入交易画像上下文）
        Content userMsg = Content.fromParts(Part.fromText(buildUserContextMessage(agentId, userId, message)));
        // 异步执行对话，获取事件流
        Flowable<Event> events = runner.runAsync(userId, sessionId, userMsg);

        // 阻塞等待所有事件完成，收集输出结果
        List<String> outputs = new ArrayList<>();
        events.blockingForEach(event -> outputs.add(event.stringifyContent()));

        return outputs;
    }

    /**
     * 流式处理消息（用于前端打字机效果）
     *
     * @return Flowable<Event> 实时事件流
     */
    @Override
    public Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        InMemoryRunner runner = aiAgentRegisterVO.getRunner();
        Content userMsg = Content.fromParts(Part.fromText(buildUserContextMessage(agentId, userId, message)));
        // 直接返回流，不阻塞，前端实时接收
        return runner.runAsync(userId, sessionId, userMsg);
    }

    /**
     * 复杂消息处理（支持：文本 + 文件 + 内联数据）
     * 用于富文本、文件上传、图片等多模态对话
     */
    @Override
    public List<String> handleMessage(ChatCommandEntity chatCommandEntity) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(chatCommandEntity.getAgentId());

        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        // 消息部件列表：支持文本、文件、图片等多种类型
        List<Part> parts = new ArrayList<>();
        // 先加入系统上下文（用户ID + 交易画像）
        parts.add(Part.fromText(buildUserContextMessage(chatCommandEntity.getAgentId(), chatCommandEntity.getUserId(), "")));

        // 1. 添加文本消息
        List<ChatCommandEntity.Content.Text> texts = chatCommandEntity.getTexts();
        if (texts != null && !texts.isEmpty()) {
            for (ChatCommandEntity.Content.Text text : texts) {
                parts.add(Part.fromText(text.getMessage()));
            }
        }

        // 2. 添加文件（URL 方式）
        List<ChatCommandEntity.Content.File> files = chatCommandEntity.getFiles();
        if (files != null && !texts.isEmpty()) {
            for (ChatCommandEntity.Content.File file : files) {
                parts.add(Part.fromUri(file.getFileUri(), file.getMimeType()));
            }
        }

        // 3. 添加内联数据（如：图片字节流）
        List<ChatCommandEntity.Content.InlineData> inlineDatas = chatCommandEntity.getInlineDatas();
        if (inlineDatas != null && !inlineDatas.isEmpty()) {
            for (ChatCommandEntity.Content.InlineData inlineData : inlineDatas) {
                parts.add(Part.fromBytes(inlineData.getBytes(), inlineData.getMimeType()));
            }
        }

        // 构建完整用户消息
        Content content = Content.builder().role("user").parts(parts).build();
        InMemoryRunner runner = aiAgentRegisterVO.getRunner();
        // 执行对话
        Flowable<Event> events = runner.runAsync(chatCommandEntity.getUserId(), chatCommandEntity.getSessionId(), content);

        // 收集结果返回
        List<String> outputs = new ArrayList<>();
        events.blockingForEach(event -> outputs.add(event.stringifyContent()));

        return outputs;
    }

    // ====================== 核心：构建用户上下文消息（自动注入交易画像） ======================

    /**
     * 构建给 AI 的用户上下文消息
     * 自动拼接：
     * 1. 用户ID
     * 2. 模拟交易画像JSON（投资顾问专用）
     * 3. 用户问题
     * 让 AI 基于真实交易数据做投顾分析
     */
    private String buildUserContextMessage(String agentId, String userId, String message) {
        StringBuilder builder = new StringBuilder();
        // 系统提示：限定AI只能查询当前用户的数据
        builder.append("系统上下文：当前用户ID为 ").append(userId)
                .append("。如需查询用户个性化交易画像，只能使用该用户ID，不要查询其他用户。");

        // 查询并添加用户交易画像（仅投资顾问智能体）
        String profileJson = querySimTradeProfileContext(agentId, userId);
        if (StringUtils.hasText(profileJson)) {
            builder.append("\n当前用户模拟交易画像JSON：").append(profileJson)
                    .append("\n请优先基于这份画像分析账户资金、持仓、近期委托、近期成交、交易行为标签和仓位集中度。");
        }

        // 添加用户问题
        if (StringUtils.hasText(message)) {
            builder.append("\n用户问题：").append(message);
        }

        return builder.toString();
    }

    // ====================== 工具：查询用户交易画像上下文 ======================

    /**
     * 查询用户模拟交易画像（仅投资顾问智能体）
     * 超长内容自动截断，避免超出AI模型限制
     */
    private String querySimTradeProfileContext(String agentId, String userId) {
        // 非投资顾问智能体，不返回画像
        if (!INVESTMENT_ADVISOR_AGENT_ID.equals(agentId)) {
            return "";
        }

        // 远程调用获取画像JSON
        String profileJson = simTradeProfilePort.queryProfileJson(userId);
        // 空或长度正常，直接返回
        if (profileJson == null || profileJson.length() <= MAX_PROFILE_CONTEXT_LENGTH) {
            return profileJson;
        }
        // 超长截断
        return profileJson.substring(0, MAX_PROFILE_CONTEXT_LENGTH) + "...[truncated]";
    }

}
