package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * DeepSeek AI 大模型服务实现类。
 * <p>基于 RestTemplate 调用 DeepSeek OpenAI 兼容接口。</p>
 */
@Slf4j
@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    /** DeepSeek API 聊天补全端点路径。 */
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
                .messages(messages)
                .build();
        return chat(request);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 对请求中未设置的字段自动填充服务端默认值。
     */
    @Override
    public String chat(DeepSeekChatRequest request) {
        fillDefaults(request);
        DeepSeekChatResponse response = chatComplete(request);

        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            log.error("DeepSeek 响应中无有效选择项");
            throw BusinessException.deepSeekApiError("响应中无有效内容");
        }

        var firstChoice = response.getChoices().getFirst();
        if (firstChoice.getMessage() == null) {
            log.error("DeepSeek 响应中消息体为空");
            throw BusinessException.deepSeekApiError("响应中消息体为空");
        }
        String content = firstChoice.getMessage().getContent();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void chatStream(List<DeepSeekChatMessage> messages, Consumer<String> onChunk) {
        DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                .messages(messages)
                .stream(true)
                .build();
        chatStream(request, onChunk);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 使用 {@link RestTemplate#execute} 配合 {@link RequestCallback} 与 {@link ResponseExtractor}
     * 实现流式 HTTP 请求。请求体由 Spring 的 Jackson MessageConverter 自动序列化，
     * 响应体通过 Hutool JSON 逐行解析 SSE 数据块。
     */
    @Override
    public void chatStream(DeepSeekChatRequest request, Consumer<String> onChunk) {
        fillDefaults(request);
        if (!Boolean.TRUE.equals(request.getStream())) {
            request.setStream(true);
        }

        String url = baseUrl + CHAT_COMPLETIONS_PATH;
        log.debug("调用 DeepSeek 流式 API，URL：{}，模型：{}", url, request.getModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.setAccept(List.of(MediaType.parseMediaType("text/event-stream"), MediaType.APPLICATION_JSON));

        RequestCallback requestCallback = clientHttpRequest -> {
            clientHttpRequest.getHeaders().addAll(headers);
            for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
                if (converter.canWrite(DeepSeekChatRequest.class, MediaType.APPLICATION_JSON)) {
                    @SuppressWarnings("unchecked")
                    HttpMessageConverter<Object> typedConverter = (HttpMessageConverter<Object>) converter;
                    typedConverter.write(request, MediaType.APPLICATION_JSON, clientHttpRequest);
                    break;
                }
            }
        };

        ResponseExtractor<Void> responseExtractor = clientHttpResponse -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientHttpResponse.getBody()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            JSONObject root = JSONUtil.parseObj(data);
                            if (root.containsKey("choices")) {
                                JSONArray choices = root.getJSONArray("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    JSONObject choice = choices.getJSONObject(0);
                                    if (choice.containsKey("delta")) {
                                        JSONObject delta = choice.getJSONObject("delta");
                                        if (delta.containsKey("content")) {
                                            String content = delta.getStr("content");
                                            if (content != null && !content.isEmpty()) {
                                                onChunk.accept(content);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("DeepSeek 流式 chunk 解析失败，跳过。原始数据：{}", data, e);
                        }
                    }
                }
            }
            return null;
        };

        restTemplate.execute(url, HttpMethod.POST, requestCallback, responseExtractor);
    }

    /**
     * 对请求中未设置的字段填充服务端默认值。
     */
    private void fillDefaults(DeepSeekChatRequest request) {
        if (request.getModel() == null || request.getModel().isBlank()) {
            request.setModel(defaultModel);
        }
        if (request.getTemperature() == null) {
            request.setTemperature(defaultTemperature);
        }
        if (request.getMaxTokens() == null) {
            request.setMaxTokens(defaultMaxTokens);
        }
        if (request.getStream() == null) {
            request.setStream(false);
        }
    }
}
