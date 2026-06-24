package com.yx.fridgebutler.dto.purchase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 将采购计划保存为模板请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavePlanAsTemplateRequest {

    /** 来源方案ID。 */
    @NotNull(message = "方案ID不能为空")
    private Long planId;

    /** 模板名称。 */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100")
    private String templateName;
}
