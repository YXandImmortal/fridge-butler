package com.yx.fridgebutler.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek 对话响应体。
 * <p>兼容 OpenAI /chat/completions 响应格式。</p>
 */
@Data
public class DeepSeekChatResponse {

    /**
     * 响应唯一标识
     */
    private String id;

    /**
     * 对象类型，通常为 chat.completion
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
    private List<DeepSeekChoice> choices;

    /**
     * Token 用量统计
     */
    private DeepSeekUsage usage;

    /**
     * 系统指纹
     */
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;
}
