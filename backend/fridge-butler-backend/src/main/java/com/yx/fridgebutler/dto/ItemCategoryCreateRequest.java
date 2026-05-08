package com.yx.fridgebutler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物品分类创建请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategoryCreateRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 30, message = "分类名称长度不能超过30")
    private String categoryName;
}
