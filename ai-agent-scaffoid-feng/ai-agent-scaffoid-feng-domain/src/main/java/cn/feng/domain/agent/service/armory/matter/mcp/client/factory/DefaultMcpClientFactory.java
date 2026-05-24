package cn.feng.domain.agent.service.armory.matter.mcp.client.factory;

import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.service.armory.matter.mcp.client.TooMcpCreateService;
import cn.feng.domain.agent.service.armory.matter.mcp.client.impl.LocalToolMcpCreateService;
import cn.feng.domain.agent.service.armory.matter.mcp.client.impl.SSEToolMcpCreateService;
import cn.feng.domain.agent.service.armory.matter.mcp.client.impl.StdioToolMcpCreateService;
import cn.feng.types.enums.ResponseCode;
import cn.feng.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class DefaultMcpClientFactory {

    @Resource
    private LocalToolMcpCreateService localToolMcpCreateService;

    @Resource
    private SSEToolMcpCreateService sseToolMcpCreateService;

    @Resource
    private StdioToolMcpCreateService stdioToolMcpCreateService;

    public TooMcpCreateService getTooMcpCreateService(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) {
        if (null != toolMcp.getLocal()) return localToolMcpCreateService;
        if (null != toolMcp.getSse()) return sseToolMcpCreateService;
        if (null != toolMcp.getStdio()) return stdioToolMcpCreateService;
        throw new AppException(ResponseCode.NOT_FOUND_METHOD.getCode(), ResponseCode.NOT_FOUND_METHOD.getInfo());
    }

}
