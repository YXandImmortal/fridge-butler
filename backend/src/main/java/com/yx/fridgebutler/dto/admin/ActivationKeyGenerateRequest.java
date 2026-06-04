package com.yx.fridgebutler.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批量生成激活密钥请求 DTO
 */
@Data
public class ActivationKeyGenerateRequest {

    /**
     * 生成数量，默认 1，最大 100
     */
    @NotNull
    @Min(1)
    @Max(100)
    private Integer count = 1;

    /**
     * 备注信息（可选）
     */
    private String remark;
}
