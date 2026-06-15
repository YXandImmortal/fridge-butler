package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保鲜评分明细维度 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreshnessDimensionVO {

    /** 维度名称 */
    private String label;

    /** 单项得分（0-100） */
    private int score;
}
