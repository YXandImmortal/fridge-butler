package com.yx.fridgebutler.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冰箱类型分布统计 VO
 * <p>用于数据看板饼图展示，包含类型名称和对应数量。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridgeTypeDistributionVO {

    /**
     * 冰箱类型名称（显示在图例、tooltip、扇区标签中）
     */
    private String name;

    /**
     * 该类型冰箱的数量（用于计算百分比和扇区大小）
     */
    private Long value;
}
