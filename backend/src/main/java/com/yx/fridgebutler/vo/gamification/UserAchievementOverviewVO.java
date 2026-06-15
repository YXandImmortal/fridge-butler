package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人中心成就总览 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementOverviewVO {

    /** 等级信息 */
    private LevelInfoVO level;

    /** 冰鲜连续天数信息 */
    private StreakInfoVO streak;

    /** 徽章列表 */
    private List<BadgeVO> badges;

    /** 今日已获得经验值 */
    private int todayExp;

    /** 今日经验值上限 */
    private int todayExpLimit;

    /** 当日保鲜评分 */
    private int freshnessScore;

    /** 当日评分等级 */
    private String scoreGrade;

    /** 四维评分明细 */
    private List<FreshnessDimensionVO> freshnessDimensions;

    /** 近90天热力图数据 */
    private List<HeatmapDayVO> heatmap;

    /** 成就系统设置 */
    private AchievementSettingsVO settings;

    /**
     * 本次查询成就总览获得的经验值（如评分突破）。
     * <p>由 overview 触发评分计算等行为时产生，若本次请求未产生新奖励则为 0。</p>
     */
    private int expGained = 0;

    /**
     * 本次查询成就总览是否触发等级升级。
     * <p>由 overview 触发评分计算导致升级时为 true，否则为 false。</p>
     */
    private boolean leveledUp = false;

    /**
     * 本次查询成就总览新解锁的徽章列表。
     * <p>由 overview 触发评分计算等行为时产生，若本次请求未解锁新徽章则为空列表。</p>
     */
    private List<BadgeUnlockInfo> badgesUnlocked = new ArrayList<>();
}
