package com.yx.fridgebutler.vo.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 聊天历史消息 VO。
 * <p>用于展示会话内的完整聊天记录，包含结构化数据以便前端正确渲染组件。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatMessageVO {

    /**
     * 消息角色：user（用户） / assistant（AI）
     */
    private String role;

    /**
     * 消息文本内容
     */
    private String content;

    /**
     * 消息类型（仅 assistant 消息有）。
     * <p>可选值：text, fridge_list, item_list, expiring_alert, recipe_recommend, trend_chart, action_confirm</p>
     */
    private String messageType;

    /**
     * 结构化数据（仅 assistant 消息有）。
     * <p>根据 messageType 有不同结构，前端据此渲染对应组件。</p>
     */
    private Object data;

    /**
     * 用户引用的附件列表（用户消息特有）。
     * <p>包含引用的冰箱或物品快照信息。</p>
     */
    private Object attachments;

    /**
     * 消息创建时间，格式：yyyy-MM-dd HH:mm:ss
     */
    private String createTime;
}
