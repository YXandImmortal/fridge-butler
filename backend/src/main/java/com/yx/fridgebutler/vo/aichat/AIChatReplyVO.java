package com.yx.fridgebutler.vo.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 聊天回复内容 VO。
 * <p>包含消息类型、自然语言文本和结构化数据，前端根据 messageType 选择渲染器。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatReplyVO {

    /**
     * 消息类型，决定前端渲染方式。
     * <p>可选值：text, fridge_list, item_list, expiring_alert,
     * recipe_recommend, trend_chart, action_confirm, fridge_creation_wizard</p>
     */
    private String messageType;

    /**
     * AI 的文本回复，可作为标题或补充说明。
     */
    private String text;

    /**
     * 结构化数据，根据 messageType 有不同结构。
     * <p>使用 Object 类型以支持不同消息类型的差异化数据结构。</p>
     */
    private Object data;
}
