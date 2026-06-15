package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热力图单日数据视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDayVO {

    /** 日期，格式 yyyy-MM-dd */
    private String date;

    /** 评分（-1 表示无数据） */
    private int score;

    /** 评分等级（无数据时为 "-"） */
    private String grade;

    /** 当日是否有过期物品 */
    private boolean hasExpired;
}
