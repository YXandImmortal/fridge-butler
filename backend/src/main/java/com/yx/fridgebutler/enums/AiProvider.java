package com.yx.fridgebutler.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum AiProvider {
    DEEPSEEK("DeepSeek", "https://api.deepseek.com",
            List.of("deepseek-v4-pro", "deepseek-flash")),
    QWEN("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1",
            List.of("qwen3.7-plus", "qwen3.8-max", "qwen3.8-flash")),
    OPENAI("OpenAI", "https://api.openai.com/v1",
            List.of("gpt-6-luna", "gpt-6.1-sol", "gpt-6-astra"));

    private final String displayName;
    private final String baseUrl;
    private final List<String> models;

    AiProvider(String displayName, String baseUrl, List<String> models) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
        this.models = models;
    }

    public String getDefaultModel() {
        return models.getFirst();
    }
}
