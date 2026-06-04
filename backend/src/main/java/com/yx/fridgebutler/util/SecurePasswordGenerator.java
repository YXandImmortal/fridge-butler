package com.yx.fridgebutler.util;

import java.security.SecureRandom;

/**
 * 安全随机密码生成工具。
 * <p>用于生成包含大小写字母、数字和特殊字符的强密码，避免使用容易被猜到的弱密码。</p>
 */
public final class SecurePasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurePasswordGenerator() {
        // 工具类禁止实例化
    }

    /**
     * 生成强随机密码。
     * <p>密码至少包含一个大写字母、一个小写字母、一个数字和一个特殊字符，
     * 其余字符随机从所有字符集中选取，最后打乱顺序。</p>
     *
     * @param length 密码长度，至少为 8
     * @return 生成的随机密码
     */
    public static String generate(int length) {
        if (length < 8) {
            length = 8;
        }

        StringBuilder sb = new StringBuilder(length);

        // 确保每类至少一个字符
        sb.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        sb.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        sb.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        // 剩余字符随机填充
        for (int i = 4; i < length; i++) {
            sb.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        // Fisher-Yates 打乱顺序
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    /**
     * 使用默认长度（16）生成强随机密码。
     *
     * @return 生成的随机密码
     */
    public static String generate() {
        return generate(16);
    }
}
