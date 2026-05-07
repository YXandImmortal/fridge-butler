package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SidebarFeatureVO {
    private Integer id;
    private String name;
    private String path;
    private String icon;
    private List<SidebarFeatureVO> children;
}
