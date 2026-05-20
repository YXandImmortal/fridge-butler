package com.yx.fridgebutler.dto.deepseek;

import lombok.Data;

/**
 * DeepSeek 流式响应中的增量消息内容。
 */
@Data
public class DeepSeekStreamDelta {

    /**
     * 消息角色（流式首包可能包含）
     */
    private String role;

    /**
     * 文本增量内容
     */
    private String content;
}
