package cn.feng.domain.agent.service.armory.node.workflow;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.feng.domain.agent.model.entity.ArmoryCommandEntity;
import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.feng.domain.agent.service.armory.AbstractArmorySupport;
import cn.feng.domain.agent.service.armory.factory.DefaultArmoryFactory;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.springai.SpringAI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("supervisorAgentNode")
public class SupervisorAgentNode extends AbstractArmorySupport {

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - SupervisorAgentNode");

        AiAgentConfigTableVO.Module.AgentWorkflow currentAgentWorkflow = dynamicContext.getCurrentAgentWorkflow();
        ChatModel chatModel = dynamicContext.getChatModel();

        List<String> subAgentNames = currentAgentWorkflow.getSubAgents();
        List<BaseAgent> subAgents = dynamicContext.queryAgentList(subAgentNames);

        LlmAgent.Builder builder = LlmAgent.builder()
                .name(currentAgentWorkflow.getName())
                .description(currentAgentWorkflow.getDescription())
                .model(new SpringAI(chatModel))
                .instruction(currentAgentWorkflow.getInstruction())
                .subAgents(subAgents);

        if (currentAgentWorkflow.getOutputKey() != null && !currentAgentWorkflow.getOutputKey().isBlank()) {
            builder.outputKey(currentAgentWorkflow.getOutputKey());
        }

        dynamicContext.getAgentGroup().put(currentAgentWorkflow.getName(), builder.build());

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return getBean("agentWorkflowNode");
    }
}
