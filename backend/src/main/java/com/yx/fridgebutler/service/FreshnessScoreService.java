package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.gamification.FreshnessScoreVO;
import com.yx.fridgebutler.vo.gamification.HeatmapDayVO;

import java.util.List;

/**
 * 保鲜评分服务接口。
 * <p>定义保鲜评分计算、热力图数据查询、历史快照管理等逻辑。</p>
 */
public interface FreshnessScoreService {

    /**
     * 计算并返回用户当日实时保鲜评分。
     * <p>首次计算时会保存快照，并触发评分突破 EXP 和满分保鲜徽章检查。</p>
     *
     * @param userId 用户ID
     * @return 保鲜评分 VO
     */
    FreshnessScoreVO calculate(Long userId);

    /**
     * 获取用户当日保鲜评分（优先使用已保存的快照）。
     *
     * @param userId 用户ID
     * @return 保鲜评分 VO
     */
    FreshnessScoreVO getTodayScore(Long userId);

    /**
     * 查询用户近 N 天的热力图数据。
     *
     * @param userId 用户ID
     * @param days   天数（90 / 180 / 365）
     * @return 热力图数据列表
     */
    List<HeatmapDayVO> getHeatmap(Long userId, int days);

    /**
     * 根据评分获取等级。
     *
     * @param score 评分
     * @return 等级（S/A/B/C/D）
     */
    String getScoreGrade(int score);
}
