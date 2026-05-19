package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 容量利用率统计响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacityStatsVO {

    /**
     * 总体平均容量利用率（百分比，0-100）
     */
    private Integer avgRate;

    /**
     * 各冰箱利用率明细列表
     */
    private List<FridgeCapacityRateVO> fridgeRates;
}
