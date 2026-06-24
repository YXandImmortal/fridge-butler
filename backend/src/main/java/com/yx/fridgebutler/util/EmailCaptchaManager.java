package com.yx.fridgebutler.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码管理器
 * <p>
 * 基于内存缓存的邮箱验证码管理工具，支持按业务类型隔离验证码，
 * 支持验证码生成、校验、发送频率限制和自动过期清理。
 * 使用 {@link ConcurrentHashMap} 保证线程安全。
 * </p>
 */
@Component
public final class EmailCaptchaManager {

    // 验证码有效期（5分钟）
    private static final long CAPTCHA_EXPIRATION_MINUTES = 5;

    // 同一邮箱发送间隔（60秒）
    private static final long SEND_INTERVAL_SECONDS = 60;

    // 验证码位数
    private static final int CAPTCHA_LENGTH = 6;

    // 邮箱验证码缓存（type:email -> 验证码信息）
    private final Map<String, CaptchaEntry> captchaCache = new ConcurrentHashMap<>();

    // 定时清理任务
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    private final Random random = new Random();

    public EmailCaptchaManager() {
        // 每5分钟清理一次过期验证码
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredCaptcha, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * 生成并存储邮箱验证码
     * <p>
     * 生成 6 位数字验证码，存入缓存，并设置定时过期任务。
     * 同时校验发送频率（同一类型同一邮箱 60 秒内只能发送一次）。
     * </p>
     *
     * @param type  业务类型，如 RESET、REGISTER、BIND
     * @param email 目标邮箱
     * @return 生成的 6 位数字验证码
     * @throws IllegalStateException 如果发送过于频繁
     */
    public String generateCaptcha(String type, String email) {
        String key = buildKey(type, email);
        long now = System.currentTimeMillis();

        CaptchaEntry existing = captchaCache.get(key);
        if (existing != null && (now - existing.sendTime()) < SEND_INTERVAL_SECONDS * 1000) {
            throw new IllegalStateException("发送过于频繁");
        }

        String captchaCode = generateNumericCaptcha();
        CaptchaEntry entry = new CaptchaEntry(captchaCode, now);
        captchaCache.put(key, entry);

        // 设置过期时间
        cleanupScheduler.schedule(() -> captchaCache.remove(key),
                CAPTCHA_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        return captchaCode;
    }

    /**
     * 验证邮箱验证码
     * <p>
     * 根据业务类型、邮箱和用户输入进行校验，验证码验证成功后会被立即移除（一次性使用）。
     * </p>
     *
     * @param type      业务类型
     * @param email     邮箱地址
     * @param userInput 用户输入的验证码
     * @return 验证成功返回 true，失败返回 false
     */
    public boolean verifyCaptcha(String type, String email, String userInput) {
        if (type == null || email == null || userInput == null) {
            return false;
        }

        String key = buildKey(type, email);
        CaptchaEntry entry = captchaCache.get(key);
        if (entry == null) {
            return false;
        }

        if (!entry.code().equals(userInput)) {
            return false;
        }

        // 验证成功后移除验证码（一次性使用）
        captchaCache.remove(key);
        return true;
    }

    /**
     * 清除指定类型和邮箱的验证码
     *
     * @param type  业务类型
     * @param email 邮箱地址
     */
    public void clearCaptcha(String type, String email) {
        if (type != null && email != null) {
            captchaCache.remove(buildKey(type, email));
        }
    }

    /**
     * 获取当前活跃的邮箱验证码数量
     *
     * @return 缓存中当前存储的验证码数量
     */
    public int getActiveCaptchaCount() {
        return captchaCache.size();
    }

    /**
     * 生成 6 位数字验证码
     *
     * @return 6 位数字字符串
     */
    private String generateNumericCaptcha() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 构建缓存 Key
     *
     * @param type  业务类型
     * @param email 邮箱地址
     * @return 格式为 type:normalizedEmail 的 key
     */
    private String buildKey(String type, String email) {
        return type + ":" + normalizeEmail(email);
    }

    /**
     * 标准化邮箱地址（去除首尾空格并转小写）
     *
     * @param email 原始邮箱
     * @return 标准化后的邮箱
     */
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    /**
     * 清理过期验证码
     * <p>
     * 兜底清理机制，当缓存中数据过多时进行清理。
     * </p>
     */
    private void cleanupExpiredCaptcha() {
        long now = System.currentTimeMillis();
        long expirationMillis = CAPTCHA_EXPIRATION_MINUTES * 60 * 1000;
        captchaCache.entrySet().removeIf(entry -> (now - entry.getValue().sendTime()) > expirationMillis);
    }

    /**
         * 验证码缓存条目
         */
        private record CaptchaEntry(String code, long sendTime) {
    }
}
