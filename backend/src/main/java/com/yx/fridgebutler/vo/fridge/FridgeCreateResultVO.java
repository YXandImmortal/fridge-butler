package com.yx.fridgebutler.vo.fridge;

import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 冰箱创建结果 VO。
 * <p>包含新创建冰箱的 ID 以及本次创建触发的奖励信息（徽章解锁、EXP）。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridgeCreateResultVO {

    /** 新创建冰箱的 ID。 */
    private Long fridgeId;

    /** 本次获得的经验值（由徽章解锁带来）。 */
    private int expGained;

    /** 是否升级。 */
    private boolean leveledUp;

    /** 升级后的当前等级（未升级时与当前等级相同）。 */
    private int currentLevel;

    /** 结算后完整等级信息。 */
    private LevelInfoVO level;

    /** 本次解锁的徽章列表。 */
    private List<BadgeUnlockInfo> badgesUnlocked;
}
