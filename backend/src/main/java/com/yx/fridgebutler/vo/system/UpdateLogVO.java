package com.yx.fridgebutler.vo.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新日志VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLogVO {

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新日期
     */
    private String date;

    /**
     * 版本一句话摘要
     */
    private String summary;

    /**
     * 变更内容列表
     */
    private List<String> changes;

    /**
     *  是否为重大更新
     */
    private Boolean isMajor;
}