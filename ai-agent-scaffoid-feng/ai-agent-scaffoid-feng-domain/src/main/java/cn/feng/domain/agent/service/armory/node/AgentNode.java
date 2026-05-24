package cn.feng.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.feng.domain.agent.model.entity.ArmoryCommandEntity;
import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.feng.domain.agent.service.armory.AbstractArmorySupport;
import cn.feng.domain.agent.service.armory.factory.DefaultArmoryFactory;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.springai.SpringAI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class AgentNode extends AbstractArmorySupport {

    @Resource
    private AgentWorkflowNode workflowNode;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - AgentNode");

        ChatModel chatModel = dynamicContext.getChatModel();

        AiAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        List<AiAgentConfigTableVO.Module.Agent> agents = aiAgentConfigTableVO.getModule().getAgents(); // 因为是构建多个agent

        // 对每一个agent进行装配
        for (AiAgentConfigTableVO.Module.Agent agentConfig : agents) {
            LlmAgent llmAgent = LlmAgent.builder()
                    .name(agentConfig.getName()) // 给 Agent 起名字
                    .description(agentConfig.getDescription()) // 写描述
                    .model(new SpringAI(chatModel)) // 配置使用的模型、mcp
                    .instruction(agentConfig.getInstruction()) // 给 AI 设定系统提示词 / 角色
                    .outputKey(agentConfig.getOutputKey()) // 指定最终输出结果存在哪个字段，方便传递上下文
                    .build();

            dynamicContext.getAgentGroup().put(agentConfig.getName(), llmAgent); // 将它们放入上下文中
        }


        return router(requestParameter, dynamicContext);

    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return workflowNode;
    }
}
