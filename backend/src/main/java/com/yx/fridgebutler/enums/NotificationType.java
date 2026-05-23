package com.yx.fridgebutler.enums;

import lombok.Getter;

/**
 * 消息通知类型枚举。
 * <p>定义系统中所有消息通知的类型标识、显示名称及默认优先级。</p>
 */
@Getter
public enum NotificationType {

    EXPIRED("已过期", 2),
    EXPIRING_CRITICAL("1天内过期", 2),
    EXPIRING_WARNING("3天内过期", 1),
    EXPIRING_NOTICE("7天内过期", 0),
    CAPACITY_WARNING("容量预警", 1),
    SYSTEM("系统通知", 0);

    /**
     * 显示名称
     */
    private final String label;

    /**
     * 默认优先级：0普通 1警告 2紧急
     */
    private final int defaultPriority;

    NotificationType(String label, int defaultPriority) {
        this.label = label;
        this.defaultPriority = defaultPriority;
    }

    /**
     * 根据字符串标识获取枚举。
     *
     * @param type 字符串标识
     * @return 对应枚举，未找到返回 null
     */
    public static NotificationType fromString(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
