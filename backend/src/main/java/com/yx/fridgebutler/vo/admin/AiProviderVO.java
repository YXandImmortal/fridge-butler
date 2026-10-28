package com.yx.fridgebutler.vo.admin;

import com.yx.fridgebutler.enums.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProviderVO {
    private AiProvider provider;
    private String providerName;
    private String baseUrl;
    private List<String> models;
}
