package com.yx.fridgebutler.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
     * 获取当前活跃的验证码数量（用于监控）
     */
    public int getActiveCaptchaCount() {
        return captchaCache.size();
    }
}