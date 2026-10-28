package com.yx.fridgebutler.dto.ai;

import com.yx.fridgebutler.enums.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiRuntimeConfig {
    private AiProvider provider;
    private String baseUrl;
    private String model;
    private String apiKey;
    private Double temperature;
    private Integer maxTokens;
}
