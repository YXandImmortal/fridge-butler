package com.yx.fridgebutler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 经验值行为类型枚举。
 * <p>定义所有可获得经验值的用户行为类型及其对应的经验值和每日上限。</p>
 */
@Getter
@AllArgsConstructor
public enum ExpActionType {

    /** 每日登录 */
    LOGIN("每日登录", 5, 1),
    /** 今日无过期 */
    NO_EXPIRE("今日无过期", 15, 1),
    /** 消耗临期食材 */
    CONSUME_EXPIRING("消耗临期食材", 10, 3),
    /** 添加新食材 */
    ADD_ITEM("添加新食材", 3, 5),
    /** 整理冰箱 */
    ORGANIZE("整理冰箱", 8, 1),
    /** 查看数据中心 */
    VIEW_DATA_CENTER("查看数据中心", 2, 1),
    /** 与 AI 对话 */
    AI_CHAT("与 AI 对话", 2, 3),
    /** 获得徽章 */
    BADGE("获得徽章", 50, null),
    /** 冰鲜连续加成 */
    STREAK_BONUS("冰鲜连续加成", null, null),
    /** 评分突破 */
    SCORE_BREAKTHROUGH("评分突破", 30, null),
    /** 月度报告 */
    MONTHLY_REPORT("查看月度报告", 20, 1),
    /** 绑定邮箱 */
    BIND_EMAIL("绑定邮箱", 50, null),
    /** 完成新手指引 */
    GUIDE("完成新手指引", 30, null),
    /** 分享成就卡片 */
    SHARE("分享成就卡片", 10, 1);

    /**
     * 行为描述。
     */
    private final String desc;

    /**
     * 单次获得经验值，null 表示动态计算或无固定值。
     */
    private final Integer exp;

    /**
     * 每日上限次数，null 表示无上限。
     */
    private final Integer dailyLimit;

    /**
     * 是否受每日总 EXP 上限（150）限制。
     * <p>所有行为均受总上限限制。</p>
     */
    public static final int DAILY_EXP_CAP = 150;
}
