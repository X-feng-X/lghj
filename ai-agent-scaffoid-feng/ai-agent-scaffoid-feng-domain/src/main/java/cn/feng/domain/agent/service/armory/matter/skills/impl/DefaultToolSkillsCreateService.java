package cn.feng.domain.agent.service.armory.matter.skills.impl;

import cn.feng.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.feng.domain.agent.service.armory.matter.skills.ToolSkillsCreateService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI Community 构建skills <a href="https://github.com/spring-ai-community/spring-ai-agent-utils">spring-ai-agent-utils</a>
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2026/2/6 08:04
 */
@Slf4j
@Service
public class DefaultToolSkillsCreateService implements ToolSkillsCreateService {

    private final PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    @Override
    public ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills) throws Exception {

        String type = toolSkills.getType();
        String path = toolSkills.getPath();

        List<ToolCallback> toolCallbackList = new ArrayList<>();

        if ("directory".equals(type)){
            ToolCallback toolCallback = SkillsTool.builder()
                    .addSkillsDirectory(path)
                    .build();
            toolCallbackList.add(toolCallback);
        }

        if ("resource".equals(type)){
            String skillsDirectory = materializeClasspathSkills(path);
            ToolCallback toolCallback = SkillsTool.builder()
                    .addSkillsDirectory(skillsDirectory)
                    .build();
            toolCallbackList.add(toolCallback);
        }

        return toolCallbackList.toArray(new ToolCallback[0]);
    }

    private String materializeClasspathSkills(String path) throws IOException {
        String normalizedPath = path.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
        Resource[] resources = resourceResolver.getResources("classpath*:" + normalizedPath + "/**/*");
        Path targetDirectory = Files.createTempDirectory("agent-skills-");
        targetDirectory.toFile().deleteOnExit();

        int copiedCount = 0;
        for (Resource resource : resources) {
            if (!resource.isReadable() || resource.getFilename() == null) {
                continue;
            }

            String relativePath = resolveRelativePath(normalizedPath, resource);
            if (relativePath.isBlank()) {
                continue;
            }

            Path targetFile = targetDirectory.resolve(relativePath).normalize();
            if (!targetFile.startsWith(targetDirectory)) {
                continue;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                Files.createDirectories(targetFile.getParent());
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
                targetFile.toFile().deleteOnExit();
                copiedCount++;
            } catch (IOException ex) {
                log.debug("Skip non-file skill resource: {}", resource.getDescription(), ex);
            }
        }

        if (copiedCount == 0) {
            throw new IOException("No readable skill resources found under classpath:" + normalizedPath);
        }

        log.info("Materialized {} classpath skill resources from {} to {}", copiedCount, normalizedPath, targetDirectory);
        return targetDirectory.toString();
    }

    private String resolveRelativePath(String rootPath, Resource resource) throws IOException {
        String url = resource.getURL().toString();
        String marker = rootPath + "/";
        int index = url.indexOf(marker);
        if (index >= 0) {
            return URLDecoder.decode(url.substring(index + marker.length()), StandardCharsets.UTF_8);
        }
        return resource.getFilename();
    }

}
