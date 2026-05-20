package com.yx.fridgebutler.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DeepSeek 流式响应中的选择项。
 */
@Data
public class DeepSeekStreamChoice {

    /**
     * 选择项索引
     */
    private Integer index;

    /**
     * 增量消息内容
     */
    private DeepSeekStreamDelta delta;

    /**
     * 生成结束原因
     */
    @JsonProperty("finish_reason")
    private String finishReason;
}
