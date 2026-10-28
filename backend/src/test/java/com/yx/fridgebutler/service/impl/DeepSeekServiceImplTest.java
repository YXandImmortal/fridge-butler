package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.ai.AiRuntimeConfig;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatMessage;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatRequest;
import com.yx.fridgebutler.enums.AiProvider;
import com.yx.fridgebutler.enums.AiRequestStatus;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.service.AiConfigService;
import com.yx.fridgebutler.service.AiRequestStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class DeepSeekServiceImplTest {

    @Mock
    private AiConfigService aiConfigService;

    @Mock
    private AiRequestStatsService requestStatsService;

    private DeepSeekServiceImpl deepSeekService;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        deepSeekService = new DeepSeekServiceImpl();
        ReflectionTestUtils.setField(deepSeekService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(deepSeekService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(deepSeekService, "aiConfigService", aiConfigService);
        ReflectionTestUtils.setField(deepSeekService, "requestStatsService", requestStatsService);
    }

    @Test
    void deepSeekRequestUsesConfiguredModelAndRecordsSuccess() {
        AiProvider provider = AiProvider.DEEPSEEK;
        useProvider(provider);
        server.expect(requestTo(provider.getBaseUrl() + "/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer sk-test"))
                .andExpect(jsonPath("$.model").value(provider.getDefaultModel()))
                .andExpect(jsonPath("$.max_tokens").value(4096))
                .andRespond(withSuccess(chatResponse("你好"), MediaType.APPLICATION_JSON));

        String result = deepSeekService.chat(chatRequest());

        assertEquals("你好", result);
        verify(requestStatsService).record(eq(provider), eq(provider.getDefaultModel()),
                eq(AiRequestStatus.SUCCESS), any(Instant.class));
        server.verify();
    }

    @Test
    void qwenRequestUsesCompatibleEndpoint() {
        AiProvider provider = AiProvider.QWEN;
        useProvider(provider);
        server.expect(requestTo(provider.getBaseUrl() + "/chat/completions"))
                .andExpect(jsonPath("$.model").value(provider.getDefaultModel()))
                .andExpect(jsonPath("$.max_tokens").value(4096))
                .andRespond(withSuccess(chatResponse("千问"), MediaType.APPLICATION_JSON));

        assertEquals("千问", deepSeekService.chat(chatRequest()));
        verify(requestStatsService).record(eq(provider), eq(provider.getDefaultModel()),
                eq(AiRequestStatus.SUCCESS), any(Instant.class));
        server.verify();
    }

    @Test
    void openAiRequestTransformsTokenAndReasoningParameters() {
        AiProvider provider = AiProvider.OPENAI;
        useProvider(provider);
        DeepSeekChatRequest request = chatRequest();
        request.setThinking(Map.of("type", "enabled"));
        request.setTopP(0.9);
        server.expect(requestTo(provider.getBaseUrl() + "/chat/completions"))
                .andExpect(jsonPath("$.model").value(provider.getDefaultModel()))
                .andExpect(jsonPath("$.max_completion_tokens").value(4096))
                .andExpect(jsonPath("$.max_tokens").doesNotExist())
                .andExpect(jsonPath("$.temperature").doesNotExist())
                .andExpect(jsonPath("$.top_p").doesNotExist())
                .andExpect(jsonPath("$.thinking").doesNotExist())
                .andRespond(withSuccess(chatResponse("OpenAI"), MediaType.APPLICATION_JSON));

        assertEquals("OpenAI", deepSeekService.chat(request));
        server.verify();
    }

    @Test
    void streamIsParsedAndRecordedOnce() {
        AiProvider provider = AiProvider.DEEPSEEK;
        useProvider(provider);
        String sse = """
                data: {"choices":[{"delta":{"content":"你"}}]}

                data: {"choices":[{"delta":{"content":"好"}}]}

                data: [DONE]

                """;
        server.expect(requestTo(provider.getBaseUrl() + "/chat/completions"))
                .andExpect(jsonPath("$.stream").value(true))
                .andRespond(withSuccess(sse, MediaType.TEXT_EVENT_STREAM));
        List<String> chunks = new ArrayList<>();
        DeepSeekChatRequest request = chatRequest();
        request.setStream(true);

        deepSeekService.chatStream(request, chunks::add);

        assertEquals(List.of("你", "好"), chunks);
        verify(requestStatsService, times(1)).record(eq(provider), eq(provider.getDefaultModel()),
                eq(AiRequestStatus.SUCCESS), any(Instant.class));
        server.verify();
    }

    @Test
    void streamCallbackFailureIsRecordedAsFailure() {
        AiProvider provider = AiProvider.DEEPSEEK;
        useProvider(provider);
        String sse = """
                data: {"choices":[{"delta":{"content":"你好"}}]}

                data: [DONE]

                """;
        server.expect(requestTo(provider.getBaseUrl() + "/chat/completions"))
                .andRespond(withSuccess(sse, MediaType.TEXT_EVENT_STREAM));
        DeepSeekChatRequest request = chatRequest();
        request.setStream(true);

        assertThrows(BusinessException.class, () -> deepSeekService.chatStream(request, chunk -> {
            throw new RuntimeException("client disconnected");
        }));

        verify(requestStatsService).record(eq(provider), eq(provider.getDefaultModel()),
                eq(AiRequestStatus.FAILURE), any(Instant.class));
        verify(requestStatsService, never()).record(eq(provider), eq(provider.getDefaultModel()),
                eq(AiRequestStatus.SUCCESS), any(Instant.class));
        server.verify();
    }

    @Test
    void retryAttemptsAreRecordedIndividually() {
        AiProvider provider = AiProvider.DEEPSEEK;
        useProvider(provider);
        server.expect(requestTo(provider.getBaseUrl() + "/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        server.expect(requestTo(provider.getBaseUrl() + "/chat/completions"))
                .andRespond(withSuccess(chatResponse("成功"), MediaType.APPLICATION_JSON));
        DeepSeekChatRequest request = chatRequest();

        assertThrows(BusinessException.class, () -> deepSeekService.chat(request));
        assertEquals("成功", deepSeekService.chat(request));

        verify(requestStatsService).record(eq(provider), eq(provider.getDefaultModel()),
                eq(AiRequestStatus.FAILURE), any(Instant.class));
        verify(requestStatsService).record(eq(provider), eq(provider.getDefaultModel()),
                eq(AiRequestStatus.SUCCESS), any(Instant.class));
        server.verify();
    }

    @Test
    void missingConfigurationDoesNotCreateRequestRecord() {
        when(aiConfigService.getRuntimeConfig()).thenThrow(BusinessException.aiConfigError("缺少密钥"));

        assertThrows(BusinessException.class, () -> deepSeekService.chat(chatRequest()));
        verifyNoInteractions(requestStatsService);
        server.verify();
    }

    private void useProvider(AiProvider provider) {
        when(aiConfigService.getRuntimeConfig()).thenReturn(new AiRuntimeConfig(
                provider, provider.getBaseUrl(), provider.getDefaultModel(), "sk-test", 0.7, 4096));
    }

    private DeepSeekChatRequest chatRequest() {
        return DeepSeekChatRequest.builder()
                .messages(List.of(DeepSeekChatMessage.builder().role("user").content("你好").build()))
                .build();
    }

    private String chatResponse(String content) {
        return """
                {"id":"chat-test","choices":[{"message":{"role":"assistant","content":"%s"}}],
                "usage":{"prompt_tokens":1,"completion_tokens":1,"total_tokens":2}}
                """.formatted(content);
    }
}
