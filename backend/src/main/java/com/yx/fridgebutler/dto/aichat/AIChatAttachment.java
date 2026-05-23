package com.yx.fridgebutler.dto.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 聊天附件 DTO。
 * <p>用户在发送消息时引用的冰箱或物品。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatAttachment {

    /**
     * 附件类型：fridge（冰箱）或 item（物品）
     */
    private String type;

    /**
     * 业务实体 ID
     */
    private Long id;

    /**
     * 实体名称（快照，用于展示）
     */
    private String name;

    /**
     * 所属冰箱 ID（物品特有）
     */
    private Long fridgeId;

    /**
     * 所属冰箱名称（物品特有）
     */
    private String fridgeName;
}
