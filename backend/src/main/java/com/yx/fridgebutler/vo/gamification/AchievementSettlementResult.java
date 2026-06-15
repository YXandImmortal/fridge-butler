package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一成就结算结果。
 * <p>包含一次业务操作产生的直接 EXP、徽章 EXP、升级状态及结算后完整等级信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementSettlementResult {

    /** 本次操作直接获得的经验值（不含徽章经验）。 */
    private int expGained;

    /** 本次解锁徽章带来的经验值合计。 */
    private int badgeExpTotal;

    /** 本次总共获得的经验值（direct + badge）。 */
    private int totalExpGained;

    /** 今日已获得经验值。 */
    private int dailyExpToday;

    /** 每日经验值上限。 */
    private int dailyExpLimit;

    /** 是否在操作 EXP + 徽章 EXP 共同作用后触发升级。 */
    private boolean leveledUp;

    /** 结算后当前等级。 */
    private int currentLevel;

    /** 结算后完整等级信息。 */
    private LevelInfoVO level;

    /** 本次新解锁的徽章列表。 */
    private List<BadgeUnlockInfo> badgesUnlocked;
}
