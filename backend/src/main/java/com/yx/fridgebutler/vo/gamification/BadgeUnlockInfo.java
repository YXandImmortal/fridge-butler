package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 徽章解锁信息。
 * <p>用于业务接口返回本次操作中新增解锁的徽章。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeUnlockInfo {

    /** 徽章唯一编码。 */
    private String code;

    /** 徽章名称。 */
    private String name;

    /** 图标字体类名（前端 iconfont 使用）。 */
    private String iconClass;

    /** 徽章描述。 */
    private String description;

    /** 徽章经验奖励。 */
    private int expReward;
}
