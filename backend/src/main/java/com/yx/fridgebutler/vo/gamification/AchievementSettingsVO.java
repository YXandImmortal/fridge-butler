package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成就系统设置 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementSettingsVO {

    /** 是否隐藏成就面板 */
    private Boolean panelHidden;

    /** 是否自动使用冰鲜保护 */
    private Boolean autoStreakProtect;

    /** 保护使用是否通知 */
    private Boolean streakProtectNotify;
}
