package com.yx.fridgebutler.vo.purchase;

import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购方案 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanVO {

    /** 方案ID。 */
    private Long id;

    /** 目标冰箱ID。 */
    private Long fridgeId;

    /** 冰箱名称。 */
    private String fridgeName;

    /** 方案名称。 */
    private String planName;

    /** 来源。 */
    private String source;

    /** 来源描述。 */
    private String sourceDesc;

    /** 状态：1=待采购, 2=已完成, 3=已取消。 */
    private Byte planStatus;

    /** 场景描述。 */
    private String sceneDesc;

    /** 物品总数。 */
    private Integer totalItems;

    /** 已完成/跳过数。 */
    private Integer completedItems;

    /** 实际入库数量。 */
    private Integer storedCount;

    /** 已采购但不入库数量。 */
    private Integer notStoredCount;

    /** 跳过数量。 */
    private Integer skippedCount;

    /** 创建时间。 */
    private Instant createTime;

    /** 更新时间。 */
    private Instant updateTime;

    /** 物品清单。 */
    private List<PurchasePlanItemVO> items;

    // ===== 游戏化奖励字段（创建计划时产生） =====

    /** 本次操作直接获得的经验值。 */
    private Integer expGained;

    /** 徽章解锁带来的经验值合计。 */
    private Integer badgeExpTotal;

    /** 今日已获得经验值。 */
    private Integer dailyExpToday;

    /** 每日经验值上限。 */
    private Integer dailyExpLimit;

    /** 是否升级。 */
    private Boolean leveledUp;

    /** 当前等级。 */
    private Integer currentLevel;

    /** 完整等级信息。 */
    private LevelInfoVO level;

    /** 新解锁徽章列表。 */
    @Builder.Default
    private List<BadgeUnlockInfo> badgesUnlocked = new ArrayList<>();
}
