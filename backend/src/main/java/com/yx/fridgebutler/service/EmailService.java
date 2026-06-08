package com.yx.fridgebutler.service;

import com.yx.fridgebutler.enums.EmailTemplate;

/**
 * 邮件服务接口。
 * <p>
 * 提供通用的邮件发送能力，支持普通文本邮件和基于模板的邮件，
 * 所有发送方法均为异步执行，避免阻塞业务请求线程。
 * </p>
 */
public interface EmailService {

    /**
     * 发送纯文本邮件。
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件正文（纯文本）
     */
    void sendTextMail(String to, String subject, String content);

    /**
     * 发送 HTML 邮件。
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param html    邮件正文（HTML 格式）
     */
    void sendHtmlMail(String to, String subject, String html);

    /**
     * 基于模板发送邮件（异步）。
     * <p>适用于批量提醒、通知等非关键场景，不阻塞主线程。</p>
     *
     * @param to       收件人邮箱
     * @param template 邮件模板枚举
     * @param args     模板占位符参数
     */
    void sendTemplateMail(String to, EmailTemplate template, Object... args);

    /**
     * 基于模板发送邮件（同步）。
     * <p>适用于验证码等关键场景，调用方需要立即知道发送结果。
     * 发送失败会抛出 {@link com.yx.fridgebutler.exception.BusinessException}。</p>
     *
     * @param to       收件人邮箱
     * @param template 邮件模板枚举
     * @param args     模板占位符参数
     */
    void sendTemplateMailSync(String to, EmailTemplate template, Object... args);

    /**
     * 检查邮件服务是否已启用。
     * <p>
     * 当邮件服务器未配置或系统关闭了邮件总开关时返回 false。
     * </p>
     *
     * @return true 表示邮件服务可用
     */
    boolean isEmailEnabled();
}
