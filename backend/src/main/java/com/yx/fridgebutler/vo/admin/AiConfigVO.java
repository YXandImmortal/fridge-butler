package com.yx.fridgebutler.vo.admin;

import com.yx.fridgebutler.enums.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConfigVO {
    private AiProvider provider;
    private String providerName;
    private String model;
    private String baseUrl;
    private boolean hasApiKey;
    private String apiKeyMasked;
}
