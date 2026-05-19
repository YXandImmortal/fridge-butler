package com.yx.fridgebutler.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DeepSeek Token 用量统计。
 */
@Data
public class DeepSeekUsage {

    /**
     * 输入（提示）token 数量
     */
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    /**
     * 输出（补全）token 数量
     */
    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    /**
     * 总 token 数量
     */
    @JsonProperty("total_tokens")
    private Integer totalTokens;

    /**
     * 输入 token 的缓存命中数量（DeepSeek 特有）
     */
    @JsonProperty("prompt_cache_hit_tokens")
    private Integer promptCacheHitTokens;

    /**
     * 输入 token 的缓存未命中数量（DeepSeek 特有）
     */
    @JsonProperty("prompt_cache_miss_tokens")
    private Integer promptCacheMissTokens;
}
