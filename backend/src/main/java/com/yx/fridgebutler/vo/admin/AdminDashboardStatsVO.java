package com.yx.fridgebutler.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理员数据看板统计 VO
 * <p>汇总展示系统核心运营指标。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsVO {

    /**
     * 用户总数
     */
    private Long userTotal;

    /**
     * 今日新增用户数
     */
    private Long userToday;

    /**
     * 冰箱总数
     */
    private Long fridgeTotal;

    /**
     * 物品总数
     */
    private Long itemTotal;

    /**
     * 今日活跃用户数（有登录记录）
     */
    private Long activeToday;

    /**
     * 冰箱类型分布统计（用于饼图展示）
     */
    private List<FridgeTypeDistributionVO> fridgeTypeDistribution;
}
