package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.gamification.BadgeVO;
import com.yx.fridgebutler.vo.gamification.FreshnessScoreVO;
import com.yx.fridgebutler.vo.gamification.MonthlyReportRewardVO;
import com.yx.fridgebutler.vo.gamification.HeatmapDayVO;
import com.yx.fridgebutler.vo.gamification.MonthlyReportVO;
import com.yx.fridgebutler.vo.gamification.UserAchievementOverviewVO;

import java.util.List;

/**
 * 游戏化成就服务接口。
 * <p>定义个人中心成就数据聚合查询逻辑。</p>
 */
public interface GamificationService {

    /**
     * 获取当前登录用户的成就总览数据。
     *
     * @return 成就总览 VO
     */
    UserAchievementOverviewVO getOverview();

    /**
     * 获取当前登录用户的徽章墙数据。
     *
     * @return 徽章列表
     */
    List<BadgeVO> getUserBadges();

    /**
     * 获取当前登录用户当日实时保鲜评分。
     *
     * @return 保鲜评分 VO
     */
    FreshnessScoreVO getFreshnessScore();

    /**
     * 获取当前登录用户近 N 天的热力图数据。
     *
     * @param days 天数（90 / 180 / 365）
     * @return 热力图数据列表
     */
    List<HeatmapDayVO> getHeatmap(int days);

    /**
     * 获取当前登录用户指定月份的月度报告。
     *
     * @param yearMonth 年月，如 2026-05
     * @return 月度报告 VO，不存在时返回 null
     */
    MonthlyReportVO getMonthlyReport(String yearMonth);

    /**
     * 标记月度报告为已查看，首次查看时发放 EXP。
     *
     * @param yearMonth 年月，如 2026-05
     * @return 本次查看奖励信息（非首次查看返回 0）
     */
    MonthlyReportRewardVO viewMonthlyReport(String yearMonth);
}
