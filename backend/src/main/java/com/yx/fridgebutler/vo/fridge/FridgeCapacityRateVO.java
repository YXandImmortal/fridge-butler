package com.yx.fridgebutler.vo.fridge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个冰箱容量利用率明细VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridgeCapacityRateVO {

    /**
     * 冰箱ID
     */
    private Long fridgeId;

    /**
     * 冰箱名称
     */
    private String fridgeName;

    /**
     * 容量利用率百分比（0-100）
     */
    private Integer rate;

    /**
     * 当前冰箱内物品数量
     */
    private Integer itemCount;

    /**
     * 冰箱总容量
     */
    private Integer totalCapacity;
}
