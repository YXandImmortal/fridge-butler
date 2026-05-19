package com.yx.fridgebutler.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek 对话请求体。
 * <p>兼容 OpenAI /chat/completions 接口格式，支持 DeepSeek 特有参数如 thinking、reasoning_effort。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeepSeekChatRequest {

    /**
     * 模型名称，例如 deepseek-v4-pro
     */
    private String model;

    /**
     * 对话消息列表
     */
    private List<DeepSeekChatMessage> messages;

    /**
     * 采样温度，控制输出的随机性，范围 0~2，默认 0.7
     */
    private Double temperature;

    /**
     * 最大生成 token 数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 是否流式输出
     */
    private Boolean stream;

    /**
     * 思考模式配置（DeepSeek 特有）。
     * <p>例如 {"type": "enabled"} 表示启用思考过程。</p>
     */
    private Map<String, Object> thinking;

    /**
     * 推理努力程度（DeepSeek 特有）：low、medium、high
     */
    @JsonProperty("reasoning_effort")
    private String reasoningEffort;

    /**
     * 替代 temperature 的核采样参数
     */
    @JsonProperty("top_p")
    private Double topP;

    /**
     * 停止生成的标识序列，最多 4 个
     */
    private List<String> stop;
}
