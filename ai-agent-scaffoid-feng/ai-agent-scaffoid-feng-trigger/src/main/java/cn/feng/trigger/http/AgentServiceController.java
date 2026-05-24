package cn.feng.trigger.http;

import cn.feng.api.IAgentService;
import cn.feng.api.dto.AiAgentConfigResponseDTO;
import cn.feng.api.dto.ChatRequestDTO;
import cn.feng.api.dto.ChatResponseDTO;
import cn.feng.api.dto.CreateSessionRequestDTO;
import cn.feng.api.dto.CreateSessionResponseDTO;
import cn.feng.api.response.Response;
import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.service.IChatService;
import cn.feng.types.enums.ResponseCode;
import cn.feng.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
@CrossOrigin(origins = "*")
public class AgentServiceController implements IAgentService {

    @Resource
    private IChatService chatService;

    @GetMapping("query_ai_agent_config_list")
    @Override
    public Response<List<AiAgentConfigResponseDTO>> queryAiAgentConfigList() {
        try {
            List<AiAgentConfigTableVO.Agent> agentConfigs = chatService.queryAiAgentConfigList();

            List<AiAgentConfigResponseDTO> responseDTOS = agentConfigs.stream().map(agentConfig -> {
                AiAgentConfigResponseDTO responseDTO = new AiAgentConfigResponseDTO();
                responseDTO.setAgentId(agentConfig.getAgentId());
                responseDTO.setAgentName(agentConfig.getAgentName());
                responseDTO.setAgentDesc(agentConfig.getAgentDesc());
                return responseDTO;
            }).collect(Collectors.toList());

            return Response.<List<AiAgentConfigResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTOS)
                    .build();
        } catch (AppException e) {
            log.error("query ai agent config list failed", e);
            return Response.<List<AiAgentConfigResponseDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("query ai agent config list failed", e);
            return Response.<List<AiAgentConfigResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    public Response<CreateSessionResponseDTO> createSession(CreateSessionRequestDTO requestDTO) {
        return doCreateSession(requestDTO);
    }

    @GetMapping("create_session")
    public Response<CreateSessionResponseDTO> createSessionByGet(@RequestBody(required = false) CreateSessionRequestDTO requestDTO,
                                                                 @RequestParam(required = false) String agentId,
                                                                 @RequestParam(required = false) String userId) {
        return doCreateSession(mergeCreateSessionRequest(requestDTO, agentId, userId));
    }

    @PostMapping("create_session")
    public Response<CreateSessionResponseDTO> createSessionByPost(@RequestBody CreateSessionRequestDTO requestDTO) {
        return doCreateSession(requestDTO);
    }

    private CreateSessionRequestDTO mergeCreateSessionRequest(CreateSessionRequestDTO requestDTO, String agentId, String userId) {
        if (requestDTO == null) {
            requestDTO = new CreateSessionRequestDTO();
        }
        if (requestDTO.getAgentId() == null || requestDTO.getAgentId().isEmpty()) {
            requestDTO.setAgentId(agentId);
        }
        if (requestDTO.getUserId() == null || requestDTO.getUserId().isEmpty()) {
            requestDTO.setUserId(userId);
        }
        return requestDTO;
    }

    private Response<CreateSessionResponseDTO> doCreateSession(CreateSessionRequestDTO requestDTO) {
        try {
            String sessionId = chatService.createSession(requestDTO.getAgentId(), requestDTO.getUserId());

            CreateSessionResponseDTO responseDTO = new CreateSessionResponseDTO();
            responseDTO.setSessionId(sessionId);

            return Response.<CreateSessionResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTO)
                    .build();
        } catch (AppException e) {
            log.error("create session failed", e);
            return Response.<CreateSessionResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("create session failed", e);
            return Response.<CreateSessionResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("chat")
    @Override
    public Response<ChatResponseDTO> chat(@RequestBody ChatRequestDTO requestDTO) {
        try {
            String sessionId = requestDTO.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = chatService.createSession(requestDTO.getAgentId(), requestDTO.getUserId());
            }

            List<String> messages = chatService.handleMessage(requestDTO.getAgentId(), requestDTO.getUserId(), sessionId, requestDTO.getMessage());

            ChatResponseDTO responseDTO = new ChatResponseDTO();
            responseDTO.setContent(String.join("\n", messages));

            return Response.<ChatResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTO)
                    .build();
        } catch (AppException e) {
            log.error("chat failed", e);
            return Response.<ChatResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("chat failed", e);
            return Response.<ChatResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("chat_stream")
    @Override
    public ResponseBodyEmitter chatStream(@RequestBody ChatRequestDTO requestDTO) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        try {
            String sessionId = requestDTO.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = chatService.createSession(requestDTO.getAgentId(), requestDTO.getUserId());
            }

            chatService.handleMessageStream(requestDTO.getAgentId(), requestDTO.getUserId(), sessionId, requestDTO.getMessage())
                    .subscribe(
                            event -> {
                                try {
                                    emitter.send(event.stringifyContent());
                                } catch (Exception e) {
                                    log.error("send stream event failed", e);
                                    emitter.completeWithError(e);
                                }
                            },
                            emitter::completeWithError,
                            emitter::complete
                    );
        } catch (Exception e) {
            log.error("chat stream failed", e);
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
