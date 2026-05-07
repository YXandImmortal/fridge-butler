package com.yx.fridgebutler.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 验证码管理器
 * <p>
 * 基于内存缓存的验证码管理工具，支持验证码的生成、校验和自动过期清理。
 * 使用 {@link ConcurrentHashMap} 保证线程安全，采用无session方式存储验证码。
 * </p>
 */
@Component
public final class CaptchaManager {

    // 验证码存储缓存（captchaId -> 验证码）
    private final Map<String, String> captchaCache = new ConcurrentHashMap<>();

    // 验证码过期时间（2分钟）
    private static final long CAPTCHA_EXPIRATION_MINUTES = 2;

    // 定时清理任务
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public CaptchaManager() {
        // 每10分钟清理一次过期验证码
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredCaptcha, 10, 10, TimeUnit.MINUTES);
    }

    /**
     * 生成验证码ID并存储验证码
     * <p>
     * 生成唯一的验证码ID，将验证码存入缓存，并设置定时过期任务。
     * </p>
     *
     * @param captchaCode 待存储的验证码内容
     * @return 生成的验证码唯一标识ID
     */
    public String generateCaptcha(String captchaCode) {
        String captchaId = UUID.randomUUID().toString();
        captchaCache.put(captchaId, captchaCode);

        // 设置过期时间（5分钟后自动删除）
        cleanupScheduler.schedule(() -> captchaCache.remove(captchaId),
                CAPTCHA_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        return captchaId;
    }

    /**
     * 验证验证码
     * <p>
     * 根据验证码ID和用户输入进行校验，验证码验证成功后会被立即移除（一次性使用）。
     * </p>
     *
     * @param captchaId  验证码ID
     * @param userInput 用户输入的验证码内容
     * @return 验证成功返回 true，失败返回 false
     */
    public boolean verifyCaptcha(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) {
            return false;
        }

        String storedCaptcha = captchaCache.get(captchaId);
        if (storedCaptcha == null) {
            return false; // 验证码不存在或已过期
        }

        // 验证成功后移除验证码（一次性使用）
        captchaCache.remove(captchaId);

        return storedCaptcha.equalsIgnoreCase(userInput);
    }

    /**
     * 清理过期验证码
     * <p>
     * 定时清理任务，当缓存中验证码数量超过阈值时清空缓存。
     * 实际过期主要由单个定时任务控制，此方法作为兜底清理机制。
     * </p>
     */
    private void cleanupExpiredCaptcha() {
        // 由于我们使用了定时任务自动删除，这里主要是清理意外残留的验证码
        // 实际应用中，可以添加更复杂的清理逻辑
        long currentTime = System.currentTimeMillis();
        // 简单的清理：如果缓存过大，清理部分旧数据
        if (captchaCache.size() > 1000) {
            captchaCache.clear();
        }
    }

    /**
     * 获取当前活跃的验证码数量
     *
     * @return 缓存中当前存储的验证码数量
     */
    public int getActiveCaptchaCount() {
        return captchaCache.size();
    }
}