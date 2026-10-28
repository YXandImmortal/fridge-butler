package com.yx.fridgebutler.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yx.fridgebutler.enums.AiProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class AiConfigUpdateRequest {

    @NotNull(message = "请选择 AI 厂商")
    private AiProvider provider;

    @NotBlank(message = "模型 ID 不能为空")
    @Size(max = 128, message = "模型 ID 不能超过 128 个字符")
    private String model;

    @Size(max = 512, message = "API 密钥长度不能超过 512 个字符")
    @ToString.Exclude
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String apiKey;
}
