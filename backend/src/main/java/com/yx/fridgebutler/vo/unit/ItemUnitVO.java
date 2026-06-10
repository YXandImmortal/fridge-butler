package com.yx.fridgebutler.vo.unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物品单位VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemUnitVO {

    /**
     * 单位ID
     */
    private Long id;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 单位类型ID
     */
    private Long unitTypeId;

    /**
     * 单位类型名称
     */
    private String unitTypeName;

    /**
     * 是否系统默认
     */
    private Boolean isSystemDefault;
}