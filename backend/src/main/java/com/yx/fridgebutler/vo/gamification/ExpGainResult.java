package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 经验值发放结果。
 * <p>记录某次 addExp 调用后实际获得的经验值、今日累计、每日上限及是否升级。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpGainResult {

    /** 本次实际获得的经验值（已受每日总上限截断）。 */
    private int expGained;

    /** 今日已获得经验值。 */
    private int dailyExpToday;

    /** 每日经验值上限。 */
    private int dailyExpLimit;

    /** 是否触发升级。 */
    private boolean leveledUp;

    /** 升级后的新等级（未升级时与当前等级相同）。 */
    private int currentLevel;
}
