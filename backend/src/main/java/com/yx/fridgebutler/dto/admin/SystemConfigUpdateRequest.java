package com.yx.fridgebutler.dto.admin;

import lombok.Data;

/**
 * 系统配置更新请求 DTO
 */
@Data
public class SystemConfigUpdateRequest {

    /**
     * 系统公告内容（支持富文本/Markdown）
     */
    private String announcement;

    /**
     * 系统简介（关于页面展示）
     */
    private String systemDescription;

    /**
     * 是否开放注册：true=开放，false=关闭
     */
    private Boolean registerOpen;

    /**
     * AI 聊天功能开关：true=开启，false=关闭
     */
    private Boolean aiChatOpen;

    /**
     * 是否需要激活密钥：true=开启，false=关闭
     */
    private Boolean requireActivationKey;

    /**
     * 管理员联系邮箱（用于用户联系管理员）
     */
    private String adminEmail;
}
