package com.yx.fridgebutler.dto.purchase;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 采购方案物品创建请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanItemCreateRequest {

    /** 物品名称。 */
    @NotBlank(message = "物品名称不能为空")
    @Size(max = 100, message = "物品名称长度不能超过100")
    private String itemName;

    /** 分类ID。 */
    private Long categoryId;

    /** 计划数量。 */
    @NotNull(message = "计划数量不能为空")
    @DecimalMin(value = "0.01", message = "计划数量必须大于0")
    private BigDecimal plannedNum;

    /** 单位ID。 */
    @NotNull(message = "单位ID不能为空")
    private Long itemUnitId;

    /** 是否建议存入冰箱。 */
    private Boolean storeInFridge;
}
