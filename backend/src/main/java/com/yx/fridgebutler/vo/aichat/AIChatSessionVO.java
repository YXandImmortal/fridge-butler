package com.yx.fridgebutler.vo.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 聊天会话列表项 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatSessionVO {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 会话标题（取用户第一条消息的前 20 字）
     */
    private String title;

    /**
     * 最后活跃时间，格式：yyyy-MM-dd HH:mm:ss
     */
    private String lastActiveTime;
}
