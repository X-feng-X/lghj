package cn.feng.domain.agent.service.armory.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.feng.domain.agent.model.entity.ArmoryCommandEntity;
import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.feng.domain.agent.service.armory.node.RootNode;
import com.google.adk.agents.BaseAgent;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认的装配工厂
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/12/17 08:16
 */
@Service
public class DefaultArmoryFactory {

    @javax.annotation.Resource
    private ApplicationContext applicationContext;

    @Resource
    private RootNode rootNode;

    public StrategyHandler<ArmoryCommandEntity, DynamicContext, AiAgentRegisterVO> armoryStrategyHandler() {
        return rootNode;
    }

    public AiAgentRegisterVO getAiAgentRegisterVO(String agentId) {
        return applicationContext.getBean(agentId, AiAgentRegisterVO.class);
    }

    /**
     * 定义一个上下文对象，用于各个节点串联的时候，写入数据和使用数据
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        /**
         * LLM API
         */
        private OpenAiApi openAiApi;

        /**
         * LLM ChatModel
         */
        private ChatModel chatModel;

        /**
         * 智能体配置组
         */
        private Map<String, BaseAgent> agentGroup = new HashMap<>(); // 这里用BaseAgent是因为在谷歌SDK中其它的Agent类型是这家伙的继承类

        private List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = new ArrayList<>();

        private AtomicInteger currentStepIndex = new AtomicInteger(0); // 线程安全的Integer

        private AiAgentConfigTableVO.Module.AgentWorkflow currentAgentWorkflow; // 当前的装配模式

        private Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }


        public List<BaseAgent> queryAgentList(List<String> agentName) {
            if (agentName == null || agentName.isEmpty()) {
                return Collections.emptyList();
            }

            List<BaseAgent> agents = new ArrayList<>();

            for (String name : agentName) {
                BaseAgent agent = agentGroup.get(name);
                if (agent != null) {
                    agents.add(agent);
                }
            }

            return agents;
        }

        /**
         * 增加步骤
         */
        public void addCurrentStepIndex() {
            currentStepIndex.incrementAndGet();
        }

        /**
         * 获取当前步长的索引
         *
         * @return 步长的索引
         */
        public int getCurrentStepIndex() {
            return currentStepIndex.get();
        }
    }

}
