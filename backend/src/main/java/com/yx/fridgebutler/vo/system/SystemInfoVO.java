package com.yx.fridgebutler.vo.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统信息响应VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemInfoVO {

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统版本
     */
    private String systemVersion;

    /**
     * 标语
     */
    private String slogan;

    /**
     * 用户首页功能列表
     */
    private List<SidebarFeatureVO> userIndexFeatures;

    /**
     * 功能特性列表
     */
    private List<FeatureVO> features;

    /**
     * 更新日志列表
     */
    private List<UpdateLogVO> updates;

    /**
     * 关于页面条目列表
     */
    private List<AboutItemVO> about;
}