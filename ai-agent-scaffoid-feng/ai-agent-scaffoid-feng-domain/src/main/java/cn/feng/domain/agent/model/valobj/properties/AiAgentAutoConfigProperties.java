package cn.feng.domain.agent.model.valobj.properties;

import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "ai.agent.config", ignoreInvalidFields = true) // 读取了yml文件中配置的内容，也就是之前的项目是通过数据库的信息来配置智能体的，但是这个项目采用了编写yml文件来配置智能体
public class AiAgentAutoConfigProperties {

    /**
     * 是否启用AI Agent自动装配
     */
    private boolean enabled = false;

    private Map<String, AiAgentConfigTableVO> tables;

}
