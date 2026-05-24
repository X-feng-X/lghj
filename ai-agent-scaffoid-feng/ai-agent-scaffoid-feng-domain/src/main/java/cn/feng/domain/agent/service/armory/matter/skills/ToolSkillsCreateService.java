package cn.feng.domain.agent.service.armory.matter.skills;

import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import org.springframework.ai.tool.ToolCallback;

/**
 * 工具 skills 构建服务
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2026/2/6 08:03
 */
public interface ToolSkillsCreateService {

    ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills) throws Exception;

}
