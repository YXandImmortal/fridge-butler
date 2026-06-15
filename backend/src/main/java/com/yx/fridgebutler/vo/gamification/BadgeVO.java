package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 徽章信息视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeVO {

    /** 徽章编码 */
    private String code;

    /** 徽章名称 */
    private String name;

    /** 图标类名 */
    private String iconClass;

    /** 徽章描述 */
    private String description;

    /** 是否已解锁 */
    private boolean unlocked;

    /** 解锁时间（未解锁时为 null） */
    private Instant unlockedAt;

    /** 解锁后获得的经验值 */
    private int expReward;

    /** 解锁条件说明 */
    private String unlockConditionDesc;
}
