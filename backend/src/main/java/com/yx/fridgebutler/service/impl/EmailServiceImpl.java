package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.enums.EmailTemplate;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.service.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类。
 * <p>
 * 基于 Spring {@link JavaMailSender} 实现邮件发送，所有发送方法均为异步执行。
 * 支持纯文本、HTML 以及模板化邮件，并为未来的邮件提醒功能预留扩展点。
 * </p>
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${spring.mail.host:}")
    private String mailHost;

    /**
     * 初始化时检查邮件配置
     */
    @PostConstruct
    public void init() {
        if (isEmailEnabled()) {
            log.info("邮件服务已启用，发件人：{}", fromAddress);
        } else {
            log.warn("邮件服务未启用：未检测到 spring.mail.host 和 spring.mail.username 配置");
        }
    }

    @Override
    public boolean isEmailEnabled() {
        return mailHost != null && !mailHost.isBlank()
                && fromAddress != null && !fromAddress.isBlank();
    }

    @Async("mailExecutor")
    @Override
    public void sendTextMail(String to, String subject, String content) {
        if (!isEmailEnabled()) {
            log.warn("邮件服务未启用，跳过发送文本邮件到：{}", to);
            return;
        }
        sendMailInternal(to, subject, content, false);
    }

    @Async("mailExecutor")
    @Override
    public void sendHtmlMail(String to, String subject, String html) {
        if (!isEmailEnabled()) {
            log.warn("邮件服务未启用，跳过发送 HTML 邮件到：{}", to);
            return;
        }
        sendMailInternal(to, subject, html, true);
    }

    @Async("mailExecutor")
    @Override
    public void sendTemplateMail(String to, EmailTemplate template, Object... args) {
        if (!isEmailEnabled()) {
            log.warn("邮件服务未启用，跳过发送模板邮件到：{}", to);
            return;
        }
        String htmlContent = template.formatContent(args)
                .replace("\n", "<br>");
        sendMailInternal(to, template.getSubject(), htmlContent, true);
    }

    @Override
    public void sendTemplateMailSync(String to, EmailTemplate template, Object... args) {
        if (!isEmailEnabled()) {
            log.error("邮件服务未启用，无法发送模板邮件到：{}", to);
            throw BusinessException.emailServiceUnavailable();
        }
        String htmlContent = template.formatContent(args)
                .replace("\n", "<br>");
        sendMailInternalSync(to, template.getSubject(), htmlContent, true);
    }

    /**
     * 内部邮件发送逻辑（同步版）
     * <p>发送失败时抛出 {@link BusinessException}，便于调用方立即感知。</p>
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     * @param isHtml  是否为 HTML 格式
     */
    private void sendMailInternalSync(String to, String subject, String content, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            mailSender.send(message);
            log.info("邮件发送成功，收件人：{}，主题：{}", to, subject);
        } catch (MailException e) {
            String msg = e.getMessage();
            // SMTP 550 等收件人相关错误（邮箱不存在、被退信）→ 400，用户可修正
            if (msg != null && (msg.contains("550") || msg.contains("recipient") || msg.contains("non-existent"))) {
                log.error("邮件被拒收，收件人：{}，主题：{}，原因：{}", to, subject, msg);
                throw BusinessException.emailSendFailed();
            }
            // 其他邮件异常（认证失败、连接超时等）→ 503，服务端问题
            log.error("邮件发送失败，收件人：{}，主题：{}，原因：{}", to, subject, msg, e);
            throw BusinessException.emailServiceUnavailable();
        } catch (MessagingException e) {
            log.error("构建邮件消息失败，收件人：{}，主题：{}，原因：{}", to, subject, e.getMessage(), e);
            throw BusinessException.emailServiceUnavailable();
        }
    }

    /**
     * 内部邮件发送逻辑（异步版）
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     * @param isHtml  是否为 HTML 格式
     */
    private void sendMailInternal(String to, String subject, String content, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            mailSender.send(message);
            log.info("邮件发送成功，收件人：{}，主题：{}", to, subject);
        } catch (MailException e) {
            log.error("邮件发送失败，收件人：{}，主题：{}，原因：{}", to, subject, e.getMessage(), e);
        } catch (MessagingException e) {
            log.error("构建邮件消息失败，收件人：{}，主题：{}，原因：{}", to, subject, e.getMessage(), e);
        }
    }
}
