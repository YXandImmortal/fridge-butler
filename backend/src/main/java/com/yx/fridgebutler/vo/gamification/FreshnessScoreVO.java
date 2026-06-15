package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保鲜评分视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreshnessScoreVO {

    /** 总评分（0-100） */
    private int score;

    /** 评分等级（S/A/B/C/D） */
    private String grade;

    /** 新鲜度维度得分（0-100） */
    private double freshnessScore;

    /** 周转效率维度得分（0-100） */
    private double turnoverScore;

    /** 过期控制维度得分（0-100） */
    private double expiredControlScore;

    /** 空间利用维度得分（0-100） */
    private double capacityScore;
}
