package com.yx.fridgebutler.vo;

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
     * 变更内容列表
     */
    private List<String> changes;
}