package com.yx.fridgebutler.dto.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 采购方案核对入库请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanSettleRequest {

    /** 核对物品列表。 */
    @NotNull(message = "核对列表不能为空")
    @Size(min = 1, message = "核对列表至少包含一项")
    @Valid
    private List<PurchasePlanSettleItemRequest> items;
}
