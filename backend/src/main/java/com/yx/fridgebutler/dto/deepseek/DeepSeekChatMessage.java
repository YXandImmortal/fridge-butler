package com.yx.fridgebutler.dto.deepseek;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeepSeek 对话消息体。
 * <p>兼容 OpenAI 格式，支持 system、user、assistant 三种角色。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekChatMessage {

    /**
     * 消息角色：system（系统提示）、user（用户输入）、assistant（模型回复）
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;
}
