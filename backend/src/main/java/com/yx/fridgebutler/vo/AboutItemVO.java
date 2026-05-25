package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关于页面条目VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AboutItemVO {

    /**
     * 标签/标题
     */
    private String label;

    /**
     * 值/内容
     */
    private String value;

    /**
     * 类型
     */
    private String type;

    /**
     *  图标
     */
    private String icon;
}