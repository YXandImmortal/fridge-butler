package com.yx.fridgebutler.dto.aichat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 聊天请求体。
 * <p>用户通过自然语言与冰箱管家进行对话交互。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatRequest {

    /**
     * 用户输入内容。
     * <p>允许只发附件不发文字，为空字符串时后端自动补充默认提示。</p>
     */
    @NotNull(message = "消息内容不能为 null")
    private String message;

    /**
     * 对话会话ID，首次可不传，后端返回后需透传。
     */
    private String sessionId;

    /**
     * 最近N轮对话上下文，用于多轮交互。
     * <p>每个消息包含 role（user/assistant）和 content。</p>
     */
    private List<AIChatHistoryMessage> history;

    /**
     * 用户引用的附件列表（可选）。
     * <p>支持引用冰箱或物品，最多建议 5 个。</p>
     */
    private List<AIChatAttachment> attachments;

    /**
     * 向导上下文（可选）。
     * <p>当用户处于多步骤向导流程中时，前端自动附加此字段。
     * 普通聊天消息中此字段为 null 或不存在。</p>
     */
    private AIChatWizardContext wizardContext;
}
