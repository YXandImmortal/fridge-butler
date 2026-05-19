package com.yx.fridgebutler.vo.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 聊天接口响应数据 VO。
 * <p>作为 {@link com.yx.fridgebutler.vo.Result} 的 data 字段内容。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatDataVO {

    /**
     * 对话会话ID，用于维持多轮对话上下文。
     */
    private String sessionId;

    /**
     * AI 回复内容，包含消息类型、文本和结构化数据。
     */
    private AIChatReplyVO reply;

    /**
     * 建议的下一步操作按钮文案列表。
     */
    private List<String> suggestions;
}
