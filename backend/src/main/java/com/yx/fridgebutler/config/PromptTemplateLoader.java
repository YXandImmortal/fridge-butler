package com.yx.fridgebutler.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI 提示词模板加载器。
 * <p>从 {@code classpath:prompts/*.md} 加载提示词模板，支持定时热刷新。</p>
 * <p>使用方式：将 {@code .md} 文件放入 {@code src/main/resources/prompts/} 目录，
  文件名（不含扩展名）即为模板 key。修改文件后最多 60 秒自动生效，无需重启服务。</p>
 */
@Slf4j
@Component
public class PromptTemplateLoader {

    /** 提示词模板文件通配路径 */
    private static final String PROMPTS_PATH = "classpath:prompts/*.md";

    /** 已加载的提示词模板缓存，key 为文件名（不含扩展名），value 为模板内容 */
    private final Map<String, String> prompts = new ConcurrentHashMap<>();
    /** 资源路径解析器 */
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    /**
     * 组件初始化：应用启动时立即加载提示词模板。
     */
    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * 定时刷新提示词模板，每 60 秒执行一次。
     * <p>开发/测试阶段可直接修改 {@code resources/prompts/} 下的文件，
     * 保存后最多等待 60 秒即可生效。</p>
     */
    @Scheduled(fixedRate = 60_000)
    public void refresh() {
        try {
            Resource[] resources = resolver.getResources(PROMPTS_PATH);
            if (resources.length == 0) {
                log.debug("未找到 prompts 目录下的 .md 文件，跳过刷新");
                return;
            }

            Map<String, String> loaded = Arrays.stream(resources)
                    .filter(Resource::isReadable)
                    .collect(Collectors.toMap(
                            this::extractKey,
                            this::readContent,
                            (a, b) -> a // 处理可能的重复 key
                    ));

            // 清除已不存在的外部模板（避免删除文件后旧内容残留）
            prompts.keySet().retainAll(loaded.keySet());
            prompts.putAll(loaded);

            log.debug("提示词模板刷新完成，共加载 {} 个：{}", loaded.size(), loaded.keySet());
        } catch (IOException e) {
            log.error("提示词模板刷新失败", e);
        }
    }

    /**
     * 获取指定 key 的提示词模板。
     *
     * @param key      模板文件名（不含 .md 后缀），如 "intent-recognition"
     * @param fallback 当外部模板不存在或为空时的内置默认值
     * @return 模板内容；若外部模板不存在则返回 fallback，保证服务可用性
     */
    public String getPrompt(String key, String fallback) {
        String prompt = prompts.get(key);
        return (prompt != null && !prompt.isBlank()) ? prompt : fallback;
    }

    /**
     * 从资源文件名中提取 key（去掉 .md 后缀）。
     */
    private String extractKey(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    /**
     * 读取资源内容。使用 InputStream 以兼容 jar 包部署。
     */
    private String readContent(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("读取提示词模板失败：{}", resource.getFilename(), e);
            return "";
        }
    }
}
