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

@Slf4j
@Service
public class ChatService implements IChatService {

    private static final String INVESTMENT_ADVISOR_AGENT_ID = "investment-advisor";
    private static final int MAX_PROFILE_CONTEXT_LENGTH = 6000;

    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    @Resource
    private AiAgentAutoConfigProperties aiAgentAutoConfigProperties;

    @Resource
    private SimTradeProfilePort simTradeProfilePort;

    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    @Override
    public List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList() {
        Map<String, AiAgentConfigTableVO> tables = aiAgentAutoConfigProperties.getTables();

        List<AiAgentConfigTableVO.Agent> agentList = new ArrayList<>();
        if (tables != null) {
            for (AiAgentConfigTableVO vo : tables.values()) {
                if (vo.getAgent() != null) {
                    agentList.add(vo.getAgent());
                }
            }
        }

        return agentList;
    }

    @Override
    public String createSession(String agentId, String userId) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        String appName = aiAgentRegisterVO.getAppName();
        InMemoryRunner runner = aiAgentRegisterVO.getRunner();

        return userSessions.computeIfAbsent(userId, uid -> {
            Session session = runner.sessionService().createSession(appName, uid).blockingGet();
            return session.id();
        });
    }

    @Override
    public List<String> handleMessage(String agentId, String userId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        String sessionId = createSession(agentId, userId);
        return handleMessage(agentId, userId, sessionId, message);
    }

    @Override
    public List<String> handleMessage(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        InMemoryRunner runner = aiAgentRegisterVO.getRunner();
        Content userMsg = Content.fromParts(Part.fromText(buildUserContextMessage(agentId, userId, message)));
        Flowable<Event> events = runner.runAsync(userId, sessionId, userMsg);

        List<String> outputs = new ArrayList<>();
        events.blockingForEach(event -> outputs.add(event.stringifyContent()));

        return outputs;
    }

    @Override
    public Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        InMemoryRunner runner = aiAgentRegisterVO.getRunner();
        Content userMsg = Content.fromParts(Part.fromText(buildUserContextMessage(agentId, userId, message)));
        return runner.runAsync(userId, sessionId, userMsg);
    }

    @Override
    public List<String> handleMessage(ChatCommandEntity chatCommandEntity) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(chatCommandEntity.getAgentId());

        if (aiAgentRegisterVO == null) {
            throw new AppException(ResponseCode.E0001.getCode());
        }

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(buildUserContextMessage(chatCommandEntity.getAgentId(), chatCommandEntity.getUserId(), "")));

        List<ChatCommandEntity.Content.Text> texts = chatCommandEntity.getTexts();
        if (texts != null && !texts.isEmpty()) {
            for (ChatCommandEntity.Content.Text text : texts) {
                parts.add(Part.fromText(text.getMessage()));
            }
        }

        List<ChatCommandEntity.Content.File> files = chatCommandEntity.getFiles();
        if (files != null && !files.isEmpty()) {
            for (ChatCommandEntity.Content.File file : files) {
                parts.add(Part.fromUri(file.getFileUri(), file.getMimeType()));
            }
        }

        List<ChatCommandEntity.Content.InlineData> inlineDatas = chatCommandEntity.getInlineDatas();
        if (inlineDatas != null && !inlineDatas.isEmpty()) {
            for (ChatCommandEntity.Content.InlineData inlineData : inlineDatas) {
                parts.add(Part.fromBytes(inlineData.getBytes(), inlineData.getMimeType()));
            }
        }

        Content content = Content.builder().role("user").parts(parts).build();
        InMemoryRunner runner = aiAgentRegisterVO.getRunner();
        Flowable<Event> events = runner.runAsync(chatCommandEntity.getUserId(), chatCommandEntity.getSessionId(), content);

        List<String> outputs = new ArrayList<>();
        events.blockingForEach(event -> outputs.add(event.stringifyContent()));

        return outputs;
    }

    private String buildUserContextMessage(String agentId, String userId, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("系统上下文：当前用户ID为 ").append(userId)
                .append("。如需查询用户个性化交易画像，只能使用该用户ID，不要查询其他用户。");

        String profileJson = querySimTradeProfileContext(agentId, userId);
        if (StringUtils.hasText(profileJson)) {
            builder.append("\n当前用户模拟交易画像JSON：").append(profileJson)
                    .append("\n请优先基于这份画像分析账户资金、持仓、近期委托、近期成交、交易行为标签和仓位集中度。");
        }

        if (StringUtils.hasText(message)) {
            builder.append("\n用户问题：").append(message);
        }

        return builder.toString();
    }

    private String querySimTradeProfileContext(String agentId, String userId) {
        if (!INVESTMENT_ADVISOR_AGENT_ID.equals(agentId)) {
            return "";
        }

        String profileJson = simTradeProfilePort.queryProfileJson(userId);
        if (profileJson == null || profileJson.length() <= MAX_PROFILE_CONTEXT_LENGTH) {
            return profileJson;
        }
        return profileJson.substring(0, MAX_PROFILE_CONTEXT_LENGTH) + "...[truncated]";
    }

}
