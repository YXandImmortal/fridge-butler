package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 等级信息 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelInfoVO {

    /** 当前等级 */
    private int currentLevel;

    /** 当前等级称号 */
    private String title;

    /** 当前等级内经验值 */
    private int currentExp;

    /** 升级到下一级所需经验值 */
    private int requiredExp;

    /** 累计总经验值 */
    private int totalExp;

    /** 等级图标 */
    private LevelIconsVO icons;

    /** 经验值进度百分比（0-100），保留两位小数 */
    private double progressPercent;

    /** 下一等级预告信息 */
    private NextLevelInfoVO nextLevel;
}
