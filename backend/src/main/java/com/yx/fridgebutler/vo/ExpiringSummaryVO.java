package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 临期/过期物品统计摘要VO。
 * <p>用于首页统计卡片展示，只统计保质期≤30天的物品。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpiringSummaryVO {

    /**
     * 临期物品数量（剩余保质期比例 < 20% 且 > 0%）。
     */
    private Integer expiringCount;

    /**
     * 已过期物品数量（剩余保质期比例 ≤ 0%）。
     */
    private Integer expiredCount;

    /**
     * 总计需要关注的物品数量（临期 + 过期）。
     */
    private Integer totalExpiring;
}
