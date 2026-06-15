package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下一等级预告信息 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextLevelInfoVO {

    /** 下一等级 */
    private int level;

    /** 下一等级称号 */
    private String title;

    /** 升到下一级所需经验值 */
    private int requiredExp;

    /** 下一等级图标 */
    private LevelIconsVO icons;
}
