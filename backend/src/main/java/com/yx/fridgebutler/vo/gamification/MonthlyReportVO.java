package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 月度报告视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportVO {

    /** 报告年月，如 2026-05 */
    private String yearMonth;

    /** 当月平均保鲜评分 */
    private int avgScore;

    /** 当月最高保鲜评分 */
    private int maxScore;

    /** 当月最低保鲜评分 */
    private int minScore;

    /** 当月过期物品数 */
    private int expiredCount;

    /** 当月临期物品数 */
    private int expiringCount;

    /** AI估算浪费金额（元） */
    private BigDecimal wastedAmount;

    /** 避免浪费食材重量（kg） */
    private BigDecimal savedKg;

    /** 减少CO2排放（kg） */
    private BigDecimal co2Saved;

    /** 节约用水（L） */
    private BigDecimal waterSaved;

    /** 当月添加物品数 */
    private int itemsAdded;

    /** 当月取出物品数 */
    private int itemsTakenOut;

    /** 当月新解锁徽章列表 */
    @Builder.Default
    private List<BadgeVO> newBadges = new ArrayList<>();

    /** 月初等级 */
    private int levelStart;

    /** 月末等级 */
    private int levelEnd;

    /** 当月最高连续天数 */
    private int streakMax;

    /** 是否已查看（用于前端展示） */
    private boolean viewed;

    /** 本次查看实际获得的经验值（当月首次为 20，否则为 0）。 */
    private int expGained;

    /** 今日已获得经验值。 */
    private int dailyExpToday;

    /** 每日经验值上限。 */
    private int dailyExpLimit;

    /** 本次查看是否在操作 EXP + 徽章 EXP 后触发升级。 */
    private boolean leveledUp;

    /** 升级后的当前等级（未升级时与当前等级相同）。 */
    private int currentLevel;

    /** 结算后完整等级信息。 */
    private LevelInfoVO level;

    /** 本次查看新解锁的徽章列表。 */
    @Builder.Default
    private List<BadgeUnlockInfo> badgesUnlocked = new ArrayList<>();
}
