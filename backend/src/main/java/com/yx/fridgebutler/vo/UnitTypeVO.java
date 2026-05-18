package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单位类型VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeVO {

    /**
     * 类型ID
     */
    private Long id;

    /**
     * 类型名称
     */
    private String unitTypeName;

    /**
     * 是否系统默认
     */
    private Boolean isSystemDefault;
}