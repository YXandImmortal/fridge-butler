package com.yx.fridgebutler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SidebarFeature {
    private Integer id;
    private String name;
    private String path;
    private String icon;
}
