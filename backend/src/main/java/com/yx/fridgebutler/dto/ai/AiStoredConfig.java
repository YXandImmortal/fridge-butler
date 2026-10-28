package com.yx.fridgebutler.dto.ai;

import com.yx.fridgebutler.enums.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStoredConfig {
    private AiProvider provider;
    private String model;
    private String apiKeyCiphertext;
    private String apiKeyMasked;
}
