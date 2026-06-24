package com.yx.fridgebutler.vo.purchase;

import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 采购方案入库结算结果 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanSettleResultVO {

    /** 方案ID。 */
    private Long planId;

    /** 实际入库数量。 */
    private int settledCount;

    /** 已采购但不入库数量。 */
    private int notStoredCount;

    /** 跳过数量。 */
    private int skippedCount;

    /** 本次操作直接获得的经验值。 */
    private int expGained;

    /** 徽章解锁带来的经验值合计。 */
    private int badgeExpTotal;

    /** 今日已获得经验值。 */
    private int dailyExpToday;

    /** 每日经验值上限。 */
    private int dailyExpLimit;

    /** 是否升级。 */
    private boolean leveledUp;

    /** 当前等级。 */
    private int currentLevel;

    /** 完整等级信息。 */
    private LevelInfoVO level;

    /** 新解锁徽章列表。 */
    @Builder.Default
    private List<BadgeUnlockInfo> badgesUnlocked = new ArrayList<>();
}
