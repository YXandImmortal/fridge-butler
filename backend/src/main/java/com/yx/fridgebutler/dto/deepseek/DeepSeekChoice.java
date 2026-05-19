package com.yx.fridgebutler.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DeepSeek 对话响应中的选择项。
 */
@Data
public class DeepSeekChoice {

    /**
     * 选择项索引
     */
    private Integer index;

    /**
     * 生成的消息
     */
    private DeepSeekChatMessage message;

    /**
     * 生成结束原因：stop、length、content_filter 等
     */
    @JsonProperty("finish_reason")
    private String finishReason;

    /**
     * 对数概率信息（如请求时指定 logprobs）
     */
    private Object logprobs;
}
