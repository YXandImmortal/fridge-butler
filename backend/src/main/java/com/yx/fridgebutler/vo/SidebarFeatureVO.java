package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 侧边栏功能菜单VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SidebarFeatureVO {

    /**
     * 菜单ID
     */
    private Integer id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 子菜单列表
     */
    private List<SidebarFeatureVO> children;
}