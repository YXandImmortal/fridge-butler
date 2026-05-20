package com.yx.fridgebutler.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek 流式响应中的单个数据块（SSE 事件）。
 * <p>兼容 OpenAI streaming 格式。</p>
 */
@Data
public class DeepSeekStreamChunk {

    /**
     * 响应唯一标识
     */
    private String id;

    /**
     * 对象类型，通常为 chat.completion.chunk
     */
    private String object;

    /**
     * 创建时间戳（Unix 时间）
     */
    private Long created;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * 生成的选择列表
     */
    private List<DeepSeekStreamChoice> choices;

    /**
     * 系统指纹
     */
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;
}
