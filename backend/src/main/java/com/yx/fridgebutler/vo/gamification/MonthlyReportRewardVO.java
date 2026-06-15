package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 月度报告查看奖励 VO。
 * <p>封装首次查看月度报告时发放的 EXP、升级状态及解锁徽章信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportRewardVO {

    /** 是否首次查看并发放奖励 */
    private boolean firstView;

    /** 本次获得 EXP，非首次为 0 */
    private int expGained;

    /** 今日已获得 EXP */
    private int dailyExpToday;

    /** 今日 EXP 上限 */
    private int dailyExpLimit;

    /** 是否升级 */
    private boolean leveledUp;

    /** 当前完整等级信息（用于升级弹窗） */
    private LevelInfoVO level;

    /** 本次解锁的徽章列表 */
    @Builder.Default
    private List<BadgeVO> badgesUnlocked = new ArrayList<>();
}
