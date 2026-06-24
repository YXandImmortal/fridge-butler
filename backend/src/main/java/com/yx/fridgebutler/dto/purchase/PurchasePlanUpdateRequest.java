package com.yx.fridgebutler.dto.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 采购方案修改请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanUpdateRequest {

    /** 方案名称。 */
    @NotBlank(message = "方案名称不能为空")
    @Size(max = 100, message = "方案名称长度不能超过100")
    private String planName;

    /** 场景描述或备注。 */
    @Size(max = 255, message = "场景描述长度不能超过255")
    private String sceneDesc;

    /** 物品清单。 */
    @NotNull(message = "物品清单不能为空")
    @Size(min = 1, message = "物品清单至少包含一项")
    @Valid
    private List<PurchasePlanItemCreateRequest> items;
}
