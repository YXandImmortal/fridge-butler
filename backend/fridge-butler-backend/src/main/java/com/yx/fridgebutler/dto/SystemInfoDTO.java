package com.yx.fridgebutler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemInfoDTO {
    private String systemName;
    private String systemVersion;
    private List<String> userIndexFeatures;
}
