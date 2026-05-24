package cn.feng.domain.agent.service.armory.node.workflow;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.feng.domain.agent.model.entity.ArmoryCommandEntity;
import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.feng.domain.agent.model.valobj.enums.AgentTypeEnum;
import cn.feng.domain.agent.service.armory.AbstractArmorySupport;
import cn.feng.domain.agent.service.armory.factory.DefaultArmoryFactory;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.ParallelAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("parallelAgentNode")
public class ParallelAgentNode extends AbstractArmorySupport {

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - ParallelAgentNode");

        AiAgentConfigTableVO.Module.AgentWorkflow currentAgentWorkflow = dynamicContext.getCurrentAgentWorkflow();

//        List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = dynamicContext.getAgentWorkflows();
//        AiAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.remove(0);

        List<String> subAgentNames = currentAgentWorkflow.getSubAgents();
        List<BaseAgent> subAgents = dynamicContext.queryAgentList(subAgentNames);

        ParallelAgent parallelAgent =
                ParallelAgent.builder()
                        .name(currentAgentWorkflow.getName())
                        .description(currentAgentWorkflow.getDescription())
                        .subAgents(subAgents)
                        .build();

        dynamicContext.getAgentGroup().put(currentAgentWorkflow.getName(), parallelAgent);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {

        return getBean("agentWorkflow");

//        List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = dynamicContext.getAgentWorkflows();
//
//        if (null == agentWorkflows || agentWorkflows.isEmpty()){
//            return defaultStrategyHandler;
//        }
//
//        AiAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.get(0);
//
//        String type = agentWorkflow.getType();
//        AgentTypeEnum agentTypeEnum = AgentTypeEnum.formType(type);
//
//        if (null == agentTypeEnum){
//            throw new RuntimeException("agentWorkflow type is error!");
//        }
//
//        String node = agentTypeEnum.getNode();
//
//        return switch (node){
//            case "loopAgentNode" -> getBean("loopAgentNode");
//            case "sequentialAgentNode" -> getBean("sequentialAgentNode");
//            default -> defaultStrategyHandler;
//        };
    }
}
