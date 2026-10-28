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
import com.yx.fridgebutler.service.AiConfigService;
import com.yx.fridgebutler.vo.admin.AiConfigVO;
import com.yx.fridgebutler.vo.admin.AiProviderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class AiConfigServiceImpl implements AiConfigService {

    private static final String KEY_AI_GLOBAL_CONFIG = "ai.global_config";

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Autowired
    private AiApiKeyCipher apiKeyCipher;

    @Value("${deepseek.api-key:}")
    private String legacyApiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String legacyBaseUrl;

    @Value("${deepseek.model:deepseek-v4-pro}")
    private String legacyModel;

    @Value("${deepseek.temperature:0.7}")
    private Double defaultTemperature;

    @Value("${deepseek.max-tokens:4096}")
    private Integer defaultMaxTokens;

    @Override
    public List<AiProviderVO> listProviders() {
        return Arrays.stream(AiProvider.values())
                .map(provider -> AiProviderVO.builder()
                        .provider(provider)
                        .providerName(provider.getDisplayName())
                        .baseUrl(provider.getBaseUrl())
                        .models(provider.getModels())
                        .build())
                .toList();
    }

    @Override
    public AiConfigVO getConfig() {
        AiStoredConfig storedConfig = readStoredConfig(findConfigEntity());
        if (storedConfig == null) {
            return AiConfigVO.builder()
                    .provider(AiProvider.DEEPSEEK)
                    .providerName(AiProvider.DEEPSEEK.getDisplayName())
                    .model(getLegacyModel())
                    .baseUrl(getLegacyBaseUrl())
                    .hasApiKey(hasText(legacyApiKey))
                    .apiKeyMasked(AiApiKeyCipher.mask(legacyApiKey))
                    .build();
        }
        return toConfigVO(storedConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiConfigVO updateConfig(AiConfigUpdateRequest request) {
        if (request == null || request.getProvider() == null) {
            throw BusinessException.aiConfigError("请选择 AI 厂商");
        }
        if (request.getModel() == null || request.getModel().isBlank()) {
            throw BusinessException.aiConfigError("模型 ID 不能为空");
        }
        String model = request.getModel().trim();
        if (model.length() > 128) {
            throw BusinessException.aiConfigError("模型 ID 不能超过 128 个字符");
        }

        SysConfig configEntity = findConfigEntity();
        AiStoredConfig current = readStoredConfig(configEntity);
        AiProvider provider = request.getProvider();
        String newApiKey = request.getApiKey() != null ? request.getApiKey().trim() : "";
        boolean providerChanged = current != null && current.getProvider() != provider;
        if (providerChanged && newApiKey.isEmpty()) {
            throw BusinessException.aiConfigError("切换厂商时必须填写" + provider.getDisplayName() + "的 API 密钥");
        }

        String ciphertext = null;
        String masked = null;
        if (current != null && current.getProvider() == provider) {
            ciphertext = current.getApiKeyCiphertext();
            masked = current.getApiKeyMasked();
        }
        if (!newApiKey.isEmpty()) {
            if (newApiKey.length() > 512) {
                throw BusinessException.aiConfigError("API 密钥长度不能超过 512 个字符");
            }
            ciphertext = apiKeyCipher.encrypt(newApiKey);
            masked = AiApiKeyCipher.mask(newApiKey);
        }

        boolean hasLegacyFallback = provider == AiProvider.DEEPSEEK && hasText(legacyApiKey);
        if (!hasText(ciphertext) && !hasLegacyFallback) {
            throw BusinessException.aiConfigError("请填写" + provider.getDisplayName() + "的 API 密钥");
        }

        AiStoredConfig updated = AiStoredConfig.builder()
                .provider(provider)
                .model(model)
                .apiKeyCiphertext(ciphertext)
                .apiKeyMasked(masked)
                .build();
        if (configEntity == null) {
            configEntity = new SysConfig();
            configEntity.setConfigKey(KEY_AI_GLOBAL_CONFIG);
        }
        configEntity.setConfigValue(JSONUtil.toJsonStr(updated));
        configEntity.setDescription("全局 AI 模型配置，API 密钥使用 AES-GCM 加密");
        configEntity.setUpdateTime(Instant.now());
        sysConfigRepository.save(configEntity);
        log.info("管理员更新全局 AI 配置，厂商：{}，模型：{}", provider, model);
        return toConfigVO(updated);
    }

    @Override
    public AiRuntimeConfig getRuntimeConfig() {
        AiStoredConfig storedConfig = readStoredConfig(findConfigEntity());
        if (storedConfig == null) {
            requireLegacyApiKey();
            return new AiRuntimeConfig(AiProvider.DEEPSEEK, getLegacyBaseUrl(), getLegacyModel(),
                    legacyApiKey.trim(), defaultTemperature, defaultMaxTokens);
        }

        String apiKey;
        if (hasText(storedConfig.getApiKeyCiphertext())) {
            apiKey = apiKeyCipher.decrypt(storedConfig.getApiKeyCiphertext());
        } else if (storedConfig.getProvider() == AiProvider.DEEPSEEK && hasText(legacyApiKey)) {
            apiKey = legacyApiKey.trim();
        } else {
            throw BusinessException.aiConfigError("当前 AI 厂商未配置可用的 API 密钥");
        }
        return new AiRuntimeConfig(storedConfig.getProvider(), storedConfig.getProvider().getBaseUrl(),
                storedConfig.getModel().trim(), apiKey, defaultTemperature, defaultMaxTokens);
    }

    private AiConfigVO toConfigVO(AiStoredConfig storedConfig) {
        boolean hasCiphertext = hasText(storedConfig.getApiKeyCiphertext());
        boolean hasLegacyFallback = storedConfig.getProvider() == AiProvider.DEEPSEEK
                && !hasCiphertext && hasText(legacyApiKey);
        String masked = hasCiphertext ? storedConfig.getApiKeyMasked()
                : hasLegacyFallback ? AiApiKeyCipher.mask(legacyApiKey) : "";
        return AiConfigVO.builder()
                .provider(storedConfig.getProvider())
                .providerName(storedConfig.getProvider().getDisplayName())
                .model(storedConfig.getModel())
                .baseUrl(storedConfig.getProvider().getBaseUrl())
                .hasApiKey(hasCiphertext || hasLegacyFallback)
                .apiKeyMasked(masked != null ? masked : "")
                .build();
    }

    private SysConfig findConfigEntity() {
        return sysConfigRepository.findByConfigKey(KEY_AI_GLOBAL_CONFIG).orElse(null);
    }

    private AiStoredConfig readStoredConfig(SysConfig configEntity) {
        if (configEntity == null || !hasText(configEntity.getConfigValue())) {
            return null;
        }
        try {
            AiStoredConfig storedConfig = JSONUtil.toBean(configEntity.getConfigValue(), AiStoredConfig.class);
            if (storedConfig == null || storedConfig.getProvider() == null
                    || !hasText(storedConfig.getModel()) || storedConfig.getModel().length() > 128) {
                throw BusinessException.aiConfigError("数据库中的 AI 配置不完整，请重新保存");
            }
            return storedConfig;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析全局 AI 配置失败，配置键：{}", KEY_AI_GLOBAL_CONFIG);
            throw BusinessException.aiConfigError("数据库中的 AI 配置格式错误，请重新保存");
        }
    }

    private String getLegacyModel() {
        return hasText(legacyModel) ? legacyModel.trim() : AiProvider.DEEPSEEK.getDefaultModel();
    }

    private String getLegacyBaseUrl() {
        String baseUrl = hasText(legacyBaseUrl) ? legacyBaseUrl.trim() : AiProvider.DEEPSEEK.getBaseUrl();
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private void requireLegacyApiKey() {
        if (!hasText(legacyApiKey)) {
            throw BusinessException.aiConfigError("尚未配置 AI API 密钥，请联系超级管理员");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
