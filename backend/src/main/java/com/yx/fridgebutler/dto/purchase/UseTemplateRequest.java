package com.yx.fridgebutler.dto.purchase;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 从模板创建采购计划请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UseTemplateRequest {

    /** 目标冰箱ID。 */
    @NotNull(message = "冰箱ID不能为空")
    private Long fridgeId;

    /** 方案名称（可选，不传则使用模板名称）。 */
    @Size(max = 100, message = "方案名称长度不能超过100")
    private String planName;
}
