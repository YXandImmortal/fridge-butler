package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.deepseek.DeepSeekChatMessage;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatRequest;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatResponse;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek AI 大模型服务实现类。
 * <p>基于 RestTemplate 调用 DeepSeek OpenAI 兼容接口。</p>
 */
@Slf4j
@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-v4-pro}")
    private String defaultModel;

    @Value("${deepseek.temperature:0.7}")
    private Double defaultTemperature;

    @Value("${deepseek.max-tokens:4096}")
    private Integer defaultMaxTokens;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public String chat(String userMessage) {
        List<DeepSeekChatMessage> messages = new ArrayList<>();
        messages.add(DeepSeekChatMessage.builder()
                .role("user")
                .content(userMessage)
                .build());
        return chat(messages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String chat(String systemMessage, String userMessage) {
        List<DeepSeekChatMessage> messages = new ArrayList<>();
        messages.add(DeepSeekChatMessage.builder()
                .role("system")
                .content(systemMessage)
                .build());
        messages.add(DeepSeekChatMessage.builder()
                .role("user")
                .content(userMessage)
                .build());
        return chat(messages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String chat(List<DeepSeekChatMessage> messages) {
        DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                .model(defaultModel)
                .messages(messages)
                .temperature(defaultTemperature)
                .maxTokens(defaultMaxTokens)
                .stream(false)
                .build();

        DeepSeekChatResponse response = chatComplete(request);

        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            log.error("DeepSeek 响应中无有效选择项");
            throw BusinessException.deepSeekApiError("响应中无有效内容");
        }

        String content = response.getChoices().getFirst().getMessage().getContent();
        log.info("DeepSeek 对话成功，消耗 token：prompt={}, completion={}, total={}",
                response.getUsage() != null ? response.getUsage().getPromptTokens() : "N/A",
                response.getUsage() != null ? response.getUsage().getCompletionTokens() : "N/A",
                response.getUsage() != null ? response.getUsage().getTotalTokens() : "N/A");
        return content;
    }

    /**
     * {@inheritDoc}
     * <p>
     * 构建 HTTP 请求头并发送 POST 请求到 /chat/completions 端点。
     * 若 DeepSeek 返回错误，将记录详细日志并抛出业务异常。
     */
    @Override
    public DeepSeekChatResponse chatComplete(DeepSeekChatRequest request) {
        String url = baseUrl + CHAT_COMPLETIONS_PATH;
        log.debug("调用 DeepSeek API，URL：{}，模型：{}", url, request.getModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<DeepSeekChatRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<DeepSeekChatResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, DeepSeekChatResponse.class);

            DeepSeekChatResponse body = response.getBody();
            if (body == null) {
                log.error("DeepSeek API 返回空响应体，HTTP 状态：{}", response.getStatusCode());
                throw BusinessException.deepSeekApiError("API 返回空响应");
            }
            return body;
        } catch (RestClientResponseException e) {
            log.error("DeepSeek API 调用失败，HTTP 状态：{}，响应体：{}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw BusinessException.deepSeekApiError("HTTP " + e.getStatusCode().value() + ": " + e.getStatusText());
        } catch (Exception e) {
            log.error("DeepSeek API 调用异常", e);
            throw BusinessException.deepSeekApiError(e.getMessage());
        }
    }
}
