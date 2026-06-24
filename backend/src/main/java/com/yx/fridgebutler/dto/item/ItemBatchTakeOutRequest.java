package com.yx.fridgebutler.dto.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量取出物品请求 DTO。
 */
@Data
public class ItemBatchTakeOutRequest {

    /**
     * 取出列表
     */
    @NotEmpty(message = "取出列表不能为空")
    @Size(max = 50, message = "单次最多批量取出50条")
    @Valid
    private List<ItemTakeOutRequest> items;

}
