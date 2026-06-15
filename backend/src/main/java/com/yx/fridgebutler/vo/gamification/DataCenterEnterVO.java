package com.yx.fridgebutler.vo.gamification;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据中心进入结果 VO。
 * <p>用户进入数据中心页面时，后端统一发放 VIEW_DATA_CENTER 经验值并触发徽章判定后返回此对象。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterEnterVO {

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

    /** 结算后完整等级信息。 */
    private LevelInfoVO level;

    /** 本次新解锁的徽章列表。 */
    private List<BadgeUnlockInfo> badgesUnlocked;
}
