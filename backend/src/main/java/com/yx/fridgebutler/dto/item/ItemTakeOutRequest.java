package com.yx.fridgebutler.dto.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 物品取出请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTakeOutRequest {

    /**
     * 物品ID
     */
    @NotNull(message = "物品ID不能为空")
    private Long id;

    /**
     * 取出数量
     */
    @NotNull(message = "取出数量不能为空")
    @DecimalMin(value = "0.01", message = "取出数量必须大于0")
    private BigDecimal takeOutNum;

}