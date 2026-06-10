package com.yx.fridgebutler.vo.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物品分类VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategoryVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 是否系统默认
     */
    private Boolean isSystemDefault;
}