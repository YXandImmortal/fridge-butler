package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冰鲜连续天数信息 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakInfoVO {

    /** 当前连续冰鲜天数 */
    private int currentStreak;

    /** 历史最高连续天数 */
    private int maxStreak;

    /** 本月剩余保护次数 */
    private int protectRemaining;

    /** 本月总保护次数 */
    private int protectTotal;

    /** 是否开启自动保护 */
    private boolean protectAutoEnabled;

    /** 冰鲜融化预警（当日有过期物品且未处理） */
    private boolean meltWarning;
}
