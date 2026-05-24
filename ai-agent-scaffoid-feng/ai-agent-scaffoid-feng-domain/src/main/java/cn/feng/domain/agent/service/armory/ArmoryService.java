package cn.feng.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.feng.domain.agent.model.entity.ArmoryCommandEntity;
import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.feng.domain.agent.service.IArmoryService;
import cn.feng.domain.agent.service.armory.factory.DefaultArmoryFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ArmoryService implements IArmoryService {

    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    @Override
    public void acceptArmoryAgents(List<AiAgentConfigTableVO> tables) throws Exception {
        for (AiAgentConfigTableVO table : tables) {
            StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> handler = defaultArmoryFactory.armoryStrategyHandler();
            handler.apply(
                    ArmoryCommandEntity.builder()
                            .aiAgentConfigTableVO(table)
                            .build(),
                    new DefaultArmoryFactory.DynamicContext());
        }
    }

}
