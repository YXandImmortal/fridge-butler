package com.yx.fridgebutler.enums;

import lombok.Getter;

/**
 * 邮件模板枚举
 * <p>
 * 定义系统中所有邮件通知的模板类型，统一管理和维护邮件主题、内容模板，
 * 便于后续扩展为基于 Velocity / Thymeleaf 的模板化邮件。
 * </p>
 */
@Getter
public enum EmailTemplate {

    /**
     * 通用邮箱验证码邮件
     * <p>可用于注册验证、忘记密码等多种场景。</p>
     */
    EMAIL_VERIFICATION(
            "邮箱验证码",
            "您的验证码为：<b>{0}</b>，有效期 5 分钟。\n" +
                    "如非本人操作，请忽略此邮件。"
    ),

    /**
     * 密码重置验证码邮件
     */
    PASSWORD_RESET(
            "密码重置验证码",
            "您正在尝试重置智鲜·引擎账号的密码，验证码为：<b>{0}</b>，有效期 5 分钟。\n" +
                    "如非本人操作，请忽略此邮件。"
    ),

    /**
     * 未来扩展：系统通知邮件
     */
    SYSTEM_NOTIFICATION(
            "系统通知",
            "{0}"
    ),

    /**
     * 未来扩展：食材临期提醒邮件
     */
    ITEM_EXPIRING_REMINDER(
            "食材临期提醒",
            "您冰箱中的以下食材即将过期，请及时处理：\n{0}"
    );

    /**
     * 邮件主题
     */
    private final String subject;

    /**
     * 邮件内容模板，使用 {0}、{1} 等占位符
     */
    private final String template;

    EmailTemplate(String subject, String template) {
        this.subject = subject;
        this.template = template;
    }

    /**
     * 使用参数填充模板内容
     *
     * @param args 替换占位符的参数
     * @return 填充后的邮件内容
     */
    public String formatContent(Object... args) {
        if (args == null || args.length == 0) {
            return this.template;
        }
        String content = this.template;
        for (int i = 0; i < args.length; i++) {
            content = content.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return content;
    }
}
