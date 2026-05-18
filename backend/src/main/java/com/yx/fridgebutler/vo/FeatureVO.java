package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能特性VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureVO {

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 图标
     */
    private String icon;
}