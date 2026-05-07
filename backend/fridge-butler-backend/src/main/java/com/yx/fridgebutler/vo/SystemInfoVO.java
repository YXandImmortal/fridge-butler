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
public class SystemInfoVO {
    private String systemName;
    private String systemVersion;
    private String slogan;
    private List<SidebarFeatureVO> userIndexFeatures;
    private List<FeatureVO> features;
    private List<UpdateLogVO> updates;
    private List<AboutItemVO> about;
}
