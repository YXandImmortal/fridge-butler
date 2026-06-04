package com.yx.fridgebutler.dto.activation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 激活密钥验证请求 DTO
 */
@Data
public class ActivationKeyVerifyRequest {

    /**
     * 激活密钥，如 FB-A3F9K2M1
     */
    @NotBlank
    private String keyCode;
}
