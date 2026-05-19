package com.yx.fridgebutler.dto.aichat;

import jakarta.validation.constraints.NotBlank;
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
     * 用户输入内容，必填。
     */
    @NotBlank(message = "消息内容不能为空")
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
}
