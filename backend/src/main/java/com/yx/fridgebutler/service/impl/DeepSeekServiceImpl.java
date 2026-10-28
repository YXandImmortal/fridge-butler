package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.yx.fridgebutler.dto.ai.AiRuntimeConfig;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatMessage;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatRequest;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatResponse;
import com.yx.fridgebutler.enums.AiProvider;
import com.yx.fridgebutler.enums.AiRequestStatus;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.service.AiConfigService;
import com.yx.fridgebutler.service.AiRequestStatsService;
import com.yx.fridgebutler.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private AiRequestStatsService requestStatsService;

    @Override
    public String chat(String userMessage) {
        List<DeepSeekChatMessage> messages = new ArrayList<>();
        messages.add(DeepSeekChatMessage.builder()
                .role("user")
                .content(userMessage)
                .build());
        return chat(messages);
    }

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

    @Override
    public String chat(List<DeepSeekChatMessage> messages) {
        DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                .messages(messages)
                .build();
        return chat(request);
    }

    @Override
    public String chat(DeepSeekChatRequest request) {
        AiRuntimeConfig config = aiConfigService.getRuntimeConfig();
        fillDefaults(request, config);
        DeepSeekChatResponse response = executeComplete(request, config);

        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            log.error("AI 响应中无有效选择项，厂商：{}，模型：{}", config.getProvider(), request.getModel());
            throw BusinessException.deepSeekApiError("响应中无有效内容");
        }
        var firstChoice = response.getChoices().getFirst();
        if (firstChoice.getMessage() == null) {
            log.error("AI 响应中消息体为空，厂商：{}，模型：{}", config.getProvider(), request.getModel());
            throw BusinessException.deepSeekApiError("响应中消息体为空");
        }
        String content = firstChoice.getMessage().getContent();
        log.info("AI 对话成功，厂商：{}，模型：{}，消耗 token：prompt={}, completion={}, total={}",
                config.getProvider(), request.getModel(),
                response.getUsage() != null ? response.getUsage().getPromptTokens() : "N/A",
                response.getUsage() != null ? response.getUsage().getCompletionTokens() : "N/A",
                response.getUsage() != null ? response.getUsage().getTotalTokens() : "N/A");
        return content;
    }

    @Override
    public DeepSeekChatResponse chatComplete(DeepSeekChatRequest request) {
        AiRuntimeConfig config = aiConfigService.getRuntimeConfig();
        fillDefaults(request, config);
        return executeComplete(request, config);
    }

    @Override
    public void chatStream(List<DeepSeekChatMessage> messages, Consumer<String> onChunk) {
        DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                .messages(messages)
                .stream(true)
                .build();
        chatStream(request, onChunk);
    }

    @Override
    public void chatStream(DeepSeekChatRequest request, Consumer<String> onChunk) {
        AiRuntimeConfig config = aiConfigService.getRuntimeConfig();
        fillDefaults(request, config);
        request.setStream(true);
        executeStream(request, config, onChunk);
    }

    private DeepSeekChatResponse executeComplete(DeepSeekChatRequest request, AiRuntimeConfig config) {
        String url = buildUrl(config.getBaseUrl());
        String requestBody = serializeRequest(request, config.getProvider());
        HttpHeaders headers = createHeaders(config.getApiKey(), false);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        Instant requestTime = Instant.now();
        log.debug("调用 AI API，厂商：{}，URL：{}，模型：{}", config.getProvider(), url, request.getModel());

        try {
            ResponseEntity<DeepSeekChatResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, DeepSeekChatResponse.class);
            DeepSeekChatResponse body = response.getBody();
            if (body == null) {
                recordRequest(config, request.getModel(), AiRequestStatus.FAILURE, requestTime);
                log.error("AI API 返回空响应体，厂商：{}，HTTP 状态：{}", config.getProvider(), response.getStatusCode());
                throw BusinessException.deepSeekApiError("API 返回空响应");
            }
            recordRequest(config, request.getModel(), AiRequestStatus.SUCCESS, requestTime);
            return body;
        } catch (BusinessException e) {
            throw e;
        } catch (RestClientResponseException e) {
            recordRequest(config, request.getModel(), AiRequestStatus.FAILURE, requestTime);
            log.error("AI API 调用失败，厂商：{}，模型：{}，HTTP 状态：{}",
                    config.getProvider(), request.getModel(), e.getStatusCode(), e);
            throw BusinessException.deepSeekApiError("HTTP " + e.getStatusCode().value() + ": " + e.getStatusText());
        } catch (Exception e) {
            recordRequest(config, request.getModel(), AiRequestStatus.FAILURE, requestTime);
            log.error("AI API 调用异常，厂商：{}，模型：{}", config.getProvider(), request.getModel(), e);
            throw BusinessException.deepSeekApiError("请求失败，请稍后重试");
        }
    }

    private void executeStream(DeepSeekChatRequest request, AiRuntimeConfig config, Consumer<String> onChunk) {
        String url = buildUrl(config.getBaseUrl());
        String requestBody = serializeRequest(request, config.getProvider());
        HttpHeaders headers = createHeaders(config.getApiKey(), true);

        RequestCallback requestCallback = clientHttpRequest -> {
            clientHttpRequest.getHeaders().addAll(headers);
            clientHttpRequest.getBody().write(requestBody.getBytes(StandardCharsets.UTF_8));
        };
        ResponseExtractor<Void> responseExtractor = clientHttpResponse -> {
            boolean completed = false;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    clientHttpResponse.getBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) {
                        completed = true;
                        break;
                    }
                    if (data.isEmpty()) {
                        continue;
                    }
                    String content = null;
                    try {
                        JSONObject root = JSONUtil.parseObj(data);
                        JSONArray choices = root.getJSONArray("choices");
                        if (choices == null || choices.isEmpty()) {
                            continue;
                        }
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject delta = choice != null ? choice.getJSONObject("delta") : null;
                        if (delta == null) {
                            continue;
                        }
                        content = delta.getStr("content");
                    } catch (Exception e) {
                        log.warn("AI 流式 chunk 解析失败，跳过。厂商：{}，模型：{}",
                                config.getProvider(), request.getModel(), e);
                    }
                    if (content != null && !content.isEmpty()) {
                        onChunk.accept(content);
                    }
                }
            }
            if (!completed) {
                throw BusinessException.deepSeekApiError("流式响应提前结束");
            }
            return null;
        };

        Instant requestTime = Instant.now();
        log.debug("调用 AI 流式 API，厂商：{}，URL：{}，模型：{}", config.getProvider(), url, request.getModel());
        try {
            restTemplate.execute(url, HttpMethod.POST, requestCallback, responseExtractor);
            recordRequest(config, request.getModel(), AiRequestStatus.SUCCESS, requestTime);
        } catch (RestClientResponseException e) {
            recordRequest(config, request.getModel(), AiRequestStatus.FAILURE, requestTime);
            log.error("AI 流式 API 调用失败，厂商：{}，模型：{}，HTTP 状态：{}",
                    config.getProvider(), request.getModel(), e.getStatusCode(), e);
            throw BusinessException.deepSeekApiError("HTTP " + e.getStatusCode().value() + ": " + e.getStatusText());
        } catch (BusinessException e) {
            recordRequest(config, request.getModel(), AiRequestStatus.FAILURE, requestTime);
            throw e;
        } catch (Exception e) {
            recordRequest(config, request.getModel(), AiRequestStatus.FAILURE, requestTime);
            log.error("AI 流式 API 调用异常，厂商：{}，模型：{}", config.getProvider(), request.getModel(), e);
            throw BusinessException.deepSeekApiError("流式请求失败，请稍后重试");
        }
    }

    private String serializeRequest(DeepSeekChatRequest request, AiProvider provider) {
        try {
            ObjectNode root = objectMapper.valueToTree(request);
            if (provider != AiProvider.DEEPSEEK) {
                root.remove("thinking");
            }
            if (provider == AiProvider.QWEN) {
                root.remove("reasoning_effort");
            }
            if (provider == AiProvider.OPENAI) {
                JsonNode maxTokens = root.remove("max_tokens");
                if (maxTokens != null && !maxTokens.isNull()) {
                    root.set("max_completion_tokens", maxTokens);
                }
                if (!"none".equalsIgnoreCase(request.getReasoningEffort())) {
                    root.remove(List.of("temperature", "top_p"));
                }
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            log.error("AI 请求序列化失败，厂商：{}，模型：{}", provider, request.getModel(), e);
            throw BusinessException.deepSeekApiError("请求序列化失败");
        }
    }

    private HttpHeaders createHeaders(String apiKey, boolean stream) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        if (stream) {
            headers.setAccept(List.of(MediaType.parseMediaType("text/event-stream"), MediaType.APPLICATION_JSON));
        }
        return headers;
    }

    private String buildUrl(String baseUrl) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalized + CHAT_COMPLETIONS_PATH;
    }

    private void fillDefaults(DeepSeekChatRequest request, AiRuntimeConfig config) {
        if (request == null) {
            throw BusinessException.deepSeekApiError("请求体不能为空");
        }
        if (request.getModel() == null || request.getModel().isBlank()) {
            request.setModel(config.getModel());
        }
        if (request.getTemperature() == null) {
            request.setTemperature(config.getTemperature());
        }
        if (request.getMaxTokens() == null) {
            request.setMaxTokens(config.getMaxTokens());
        }
        if (request.getStream() == null) {
            request.setStream(false);
        }
    }

    private void recordRequest(AiRuntimeConfig config, String model, AiRequestStatus status, Instant requestTime) {
        try {
            requestStatsService.record(config.getProvider(), model, status, requestTime);
        } catch (Exception e) {
            log.error("AI 请求统计记录异常，厂商：{}，模型：{}，状态：{}", config.getProvider(), model, status, e);
        }
    }
}
