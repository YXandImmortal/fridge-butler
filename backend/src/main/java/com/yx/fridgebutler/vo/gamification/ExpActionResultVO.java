package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用 EXP 行为结果 VO。
 * <p>适用于原本返回 Void 的用户/业务操作接口，统一携带经验值与徽章解锁信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpActionResultVO {

    /** 本次操作实际获得的经验值。 */
    private int expGained;

    /** 今日已获得经验值。 */
    private int dailyExpToday;

    /** 每日经验值上限。 */
    private int dailyExpLimit;

    /** 是否在操作 EXP + 徽章 EXP 后触发升级。 */
    private boolean leveledUp;

    /** 升级后的当前等级（未升级时与当前等级相同）。 */
    private int currentLevel;

    /** 结算后完整等级信息。 */
    private LevelInfoVO level;

    /** 本次操作新解锁的徽章列表。 */
    @Builder.Default
    private List<BadgeUnlockInfo> badgesUnlocked = new ArrayList<>();
}
