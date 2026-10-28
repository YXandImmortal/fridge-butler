package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONUtil;
import com.yx.fridgebutler.dto.admin.AiConfigUpdateRequest;
import com.yx.fridgebutler.dto.ai.AiRuntimeConfig;
import com.yx.fridgebutler.dto.ai.AiStoredConfig;
import com.yx.fridgebutler.entity.SysConfig;
import com.yx.fridgebutler.enums.AiProvider;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysConfigRepository;
import com.yx.fridgebutler.security.AiApiKeyCipher;
import com.yx.fridgebutler.vo.admin.AiConfigVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiConfigServiceImplTest {

    private static final String CONFIG_KEY = "ai.global_config";

    @Mock
    private SysConfigRepository sysConfigRepository;

    @Mock
    private AiApiKeyCipher apiKeyCipher;

    @InjectMocks
    private AiConfigServiceImpl aiConfigService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiConfigService, "legacyApiKey", "sk-legacy-1234");
        ReflectionTestUtils.setField(aiConfigService, "legacyBaseUrl", "https://api.deepseek.com");
        ReflectionTestUtils.setField(aiConfigService, "legacyModel", "deepseek-v4-pro");
        ReflectionTestUtils.setField(aiConfigService, "defaultTemperature", 0.7);
        ReflectionTestUtils.setField(aiConfigService, "defaultMaxTokens", 4096);
    }

    @Test
    void fallsBackToLegacyDeepSeekConfig() {
        when(sysConfigRepository.findByConfigKey(CONFIG_KEY)).thenReturn(Optional.empty());

        AiConfigVO result = aiConfigService.getConfig();

        assertEquals(AiProvider.DEEPSEEK, result.getProvider());
        assertEquals("deepseek-v4-pro", result.getModel());
        assertTrue(result.isHasApiKey());
        assertEquals("****1234", result.getApiKeyMasked());
        verifyNoInteractions(apiKeyCipher);
    }

    @Test
    void blankKeyPreservesCurrentProviderCiphertext() {
        AiStoredConfig stored = storedConfig(AiProvider.QWEN, "qwen3.7-plus", "encrypted", "****cret");
        SysConfig entity = configEntity(stored);
        when(sysConfigRepository.findByConfigKey(CONFIG_KEY)).thenReturn(Optional.of(entity));
        AiConfigUpdateRequest request = updateRequest(AiProvider.QWEN, "qwen3.8-max", "");

        AiConfigVO result = aiConfigService.updateConfig(request);

        assertEquals("qwen3.8-max", result.getModel());
        assertTrue(result.isHasApiKey());
        assertEquals("****cret", result.getApiKeyMasked());
        verify(apiKeyCipher, never()).encrypt(any());
        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(sysConfigRepository).save(captor.capture());
        assertTrue(captor.getValue().getConfigValue().contains("encrypted"));
    }

    @Test
    void newKeyIsEncryptedAndNeverReturnedOrStoredAsPlaintext() {
        when(sysConfigRepository.findByConfigKey(CONFIG_KEY)).thenReturn(Optional.empty());
        when(apiKeyCipher.encrypt("sk-qwen-secret")).thenReturn("encrypted-value");
        AiConfigUpdateRequest request = updateRequest(AiProvider.QWEN, "qwen3.7-plus", "sk-qwen-secret");

        AiConfigVO result = aiConfigService.updateConfig(request);

        assertEquals("****cret", result.getApiKeyMasked());
        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(sysConfigRepository).save(captor.capture());
        String savedJson = captor.getValue().getConfigValue();
        assertTrue(savedJson.contains("encrypted-value"));
        assertFalse(savedJson.contains("sk-qwen-secret"));
        assertEquals(AiProvider.QWEN, result.getProvider());
        assertTrue(result.isHasApiKey());
    }

    @Test
    void switchingProviderRequiresNewKey() {
        SysConfig entity = configEntity(storedConfig(AiProvider.DEEPSEEK, "deepseek-v4-pro", "encrypted", "****1234"));
        when(sysConfigRepository.findByConfigKey(CONFIG_KEY)).thenReturn(Optional.of(entity));

        assertThrows(BusinessException.class,
                () -> aiConfigService.updateConfig(updateRequest(AiProvider.QWEN, "qwen3.7-plus", "")));
        verify(sysConfigRepository, never()).save(any());
    }

    @Test
    void runtimeConfigDecryptsSavedKey() {
        SysConfig entity = configEntity(storedConfig(AiProvider.OPENAI, "gpt-6-luna", "encrypted", "****1234"));
        when(sysConfigRepository.findByConfigKey(CONFIG_KEY)).thenReturn(Optional.of(entity));
        when(apiKeyCipher.decrypt("encrypted")).thenReturn("sk-openai-1234");

        AiRuntimeConfig result = aiConfigService.getRuntimeConfig();

        assertEquals(AiProvider.OPENAI, result.getProvider());
        assertEquals("gpt-6-luna", result.getModel());
        assertEquals("sk-openai-1234", result.getApiKey());
        assertEquals("https://api.openai.com/v1", result.getBaseUrl());
    }

    private AiStoredConfig storedConfig(AiProvider provider, String model, String ciphertext, String masked) {
        return AiStoredConfig.builder()
                .provider(provider)
                .model(model)
                .apiKeyCiphertext(ciphertext)
                .apiKeyMasked(masked)
                .build();
    }

    private SysConfig configEntity(AiStoredConfig storedConfig) {
        SysConfig entity = new SysConfig();
        entity.setConfigKey(CONFIG_KEY);
        entity.setConfigValue(JSONUtil.toJsonStr(storedConfig));
        return entity;
    }

    private AiConfigUpdateRequest updateRequest(AiProvider provider, String model, String apiKey) {
        AiConfigUpdateRequest request = new AiConfigUpdateRequest();
        request.setProvider(provider);
        request.setModel(model);
        request.setApiKey(apiKey);
        return request;
    }
}
