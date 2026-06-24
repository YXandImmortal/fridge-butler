package com.yx.fridgebutler.util;

import java.util.regex.Pattern;

/**
 * 邮箱工具类。
 * <p>提供邮箱格式校验等通用能力，避免正则规则散落在各业务类中。</p>
 */
public final class EmailUtil {

    /**
     * 基础邮箱格式正则。
     * <p>仅用于前端提示、快速校验等场景；不保证邮箱真实可达。</p>
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private EmailUtil() {
        // 工具类禁止实例化
    }

    /**
     * 校验邮箱格式是否合法。
     *
     * @param email 待校验的邮箱地址
     * @return true 表示非空且格式合法
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
