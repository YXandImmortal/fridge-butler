package com.yx.fridgebutler.dto.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 聊天历史消息。
 * <p>用于在多轮对话中传递上下文信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatHistoryMessage {

    /**
     * 消息角色：user（用户输入）、assistant（模型回复）
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;
}
