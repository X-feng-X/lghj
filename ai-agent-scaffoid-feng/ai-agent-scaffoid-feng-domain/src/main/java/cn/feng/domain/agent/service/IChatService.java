package cn.feng.domain.agent.service;

import cn.feng.domain.agent.model.entity.ChatCommandEntity;
import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Flowable;

import java.util.List;

/**
 * 对话接口
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/12/17 08:13
 */
public interface IChatService {

    /**
     * 展示现在注册了哪些agent
     *
     * @return
     */
    List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList();

    /**
     * 创建会话
     *
     * @param agentId
     * @param userId
     * @return
     */
    String createSession(String agentId, String userId);

    /**
     * 传递消息
     *
     * @param agentId
     * @param userId
     * @param message
     * @return
     */
    List<String> handleMessage(String agentId, String userId, String message);

    /**
     * 传递消息
     *
     * @param agentId
     * @param userId
     * @param sessionId
     * @param message
     * @return
     */
    List<String> handleMessage(String agentId, String userId, String sessionId, String message);

    /**
     * 流式返回消息
     *
     * @param agentId
     * @param userId
     * @param sessionId
     * @param message
     * @return
     */
    Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message);

    List<String> handleMessage(ChatCommandEntity chatCommandEntity);

}
