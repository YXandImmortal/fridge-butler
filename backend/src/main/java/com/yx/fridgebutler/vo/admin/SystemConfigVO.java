package com.yx.fridgebutler.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统配置 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigVO {

    /**
     * 系统公告内容
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
     * <p>开启后，新注册的普通用户必须先输入有效的激活密钥才能使用系统功能。</p>
     */
    private Boolean requireActivationKey;

    /**
     * 管理员联系邮箱（用于用户联系管理员）
     */
    private String adminEmail;
}
