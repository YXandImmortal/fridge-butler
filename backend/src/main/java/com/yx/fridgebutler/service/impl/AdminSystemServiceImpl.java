package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.admin.SystemConfigUpdateRequest;
import com.yx.fridgebutler.entity.SysConfig;
import com.yx.fridgebutler.repository.SysConfigRepository;
import com.yx.fridgebutler.service.AdminSystemService;
import com.yx.fridgebutler.vo.admin.SystemConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * 管理员系统配置服务实现类
 * <p>以 key-value 形式管理配置项，支持自动初始化默认值。</p>
 */
@Slf4j
@Service
public class AdminSystemServiceImpl implements AdminSystemService {

    /** 系统公告配置键。 */
    private static final String KEY_ANNOUNCEMENT = "sys.announcement";
    /** 系统简介配置键。 */
    private static final String KEY_SYSTEM_DESCRIPTION = "sys.system_description";
    /** 是否开放注册配置键。 */
    private static final String KEY_REGISTER_OPEN = "sys.register_open";
    /** AI聊天功能开关配置键。 */
    private static final String KEY_AI_CHAT_OPEN = "sys.ai_chat_open";
    /** 是否需要激活密钥配置键。 */
    private static final String KEY_REQUIRE_ACTIVATION = "sys.require_activation_key";
    /** 管理员联系邮箱配置键。 */
    private static final String KEY_ADMIN_EMAIL = "sys.admin_email";

    @Autowired
    private SysConfigRepository sysConfigRepository;

    /**
     * {@inheritDoc}
     * <p>从数据库读取各配置项，不存在时返回默认值。</p>
     */
    @Override
    public SystemConfigVO getSystemConfig() {
        return SystemConfigVO.builder()
                .announcement(getConfigValue(KEY_ANNOUNCEMENT, ""))
                .systemDescription(getConfigValue(KEY_SYSTEM_DESCRIPTION, ""))
                .registerOpen(Boolean.parseBoolean(getConfigValue(KEY_REGISTER_OPEN, "true")))
                .aiChatOpen(Boolean.parseBoolean(getConfigValue(KEY_AI_CHAT_OPEN, "true")))
                .requireActivationKey(Boolean.parseBoolean(getConfigValue(KEY_REQUIRE_ACTIVATION, "false")))
                .adminEmail(getConfigValue(KEY_ADMIN_EMAIL, ""))
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>更新配置项，不存在时自动创建。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSystemConfig(SystemConfigUpdateRequest request) {
        if (request.getAnnouncement() != null) {
            saveConfig(KEY_ANNOUNCEMENT, request.getAnnouncement(), "系统公告内容");
        }
        if (request.getSystemDescription() != null) {
            saveConfig(KEY_SYSTEM_DESCRIPTION, request.getSystemDescription(), "系统简介");
        }
        if (request.getRegisterOpen() != null) {
            saveConfig(KEY_REGISTER_OPEN, String.valueOf(request.getRegisterOpen()), "是否开放注册");
        }
        if (request.getAiChatOpen() != null) {
            saveConfig(KEY_AI_CHAT_OPEN, String.valueOf(request.getAiChatOpen()), "AI聊天功能开关");
        }
        if (request.getRequireActivationKey() != null) {
            saveConfig(KEY_REQUIRE_ACTIVATION, String.valueOf(request.getRequireActivationKey()), "是否需要激活密钥");
        }
        if (request.getAdminEmail() != null) {
            saveConfig(KEY_ADMIN_EMAIL, request.getAdminEmail(), "管理员联系邮箱");
        }
        log.info("管理员更新系统配置成功");
    }

    /**
     * 读取指定配置键的值，不存在时返回默认值并自动创建。
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private String getConfigValue(String key, String defaultValue) {
        Optional<SysConfig> configOpt = sysConfigRepository.findByConfigKey(key);
        if (configOpt.isPresent()) {
            return configOpt.get().getConfigValue();
        }
        // 不存在时自动初始化默认值
        SysConfig config = new SysConfig();
        config.setConfigKey(key);
        config.setConfigValue(defaultValue);
        config.setUpdateTime(Instant.now());
        sysConfigRepository.save(config);
        return defaultValue;
    }

    /**
     * 保存或更新配置项
     *
     * @param key         配置键
     * @param value       配置值
     * @param description 描述
     */
    private void saveConfig(String key, String value, String description) {
        SysConfig config = sysConfigRepository.findByConfigKey(key)
                .orElse(new SysConfig());
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setDescription(description);
        config.setUpdateTime(Instant.now());
        sysConfigRepository.save(config);
    }
}
