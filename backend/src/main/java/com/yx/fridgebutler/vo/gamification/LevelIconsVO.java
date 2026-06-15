package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 等级图标展示 VO。
 * <p>采用 4 进制递进规则：雪人(64) → 冰淇淋(16) → 冰块(4) → 雪花(1)。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelIconsVO {

    /** 雪人数量（每 64 级） */
    private int snowman;

    /** 冰淇淋数量（每 16 级） */
    private int iceCream;

    /** 冰块数量（每 4 级） */
    private int ice;

    /** 雪花数量（每 1 级） */
    private int snowflake;
}
