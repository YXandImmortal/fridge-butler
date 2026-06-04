package com.yx.fridgebutler.enums;

import lombok.Getter;

/**
 * 每日小贴士类型枚举。
 */
@Getter
public enum DailyTipType {

    FACT("冷知识"),
    TIP("实用技巧"),
    JOKE("冷笑话"),
    RIDDLE("谜语");

    /**
     * 中文显示名称
     */
    private final String label;

    DailyTipType(String label) {
        this.label = label;
    }

    /**
     * 根据英文标识获取枚举。
     *
     * @param type 英文标识
     * @return 对应枚举，未找到返回 null
     */
    public static DailyTipType fromString(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
