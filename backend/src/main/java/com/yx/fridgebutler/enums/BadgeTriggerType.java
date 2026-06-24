package com.yx.fridgebutler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 徽章触发类型枚举。
 * <p>定义所有可能触发徽章检查的业务行为类型。</p>
 */
@Getter
@AllArgsConstructor
public enum BadgeTriggerType {

    /** 添加食材 */
    ADD_ITEM("添加食材"),
    /** 取出食材 */
    TAKE_OUT_ITEM("取出食材"),
    /** 取出临期食材 */
    TAKE_OUT_EXPIRING("取出临期食材"),
    /** 更新食材 */
    UPDATE_ITEM("更新食材"),
    /** 创建冰箱 */
    CREATE_FRIDGE("创建冰箱"),
    /** AI对话（通用） */
    AI_CHAT("AI对话"),
    /** AI菜谱推荐 */
    AI_RECIPE("AI菜谱推荐"),
    /** 查看数据中心 */
    VIEW_DATA_CENTER("查看数据中心"),
    /** 登录 */
    LOGIN("登录"),
    /** 绑定邮箱 */
    BIND_EMAIL("绑定邮箱"),
    /** 完成指引 */
    COMPLETE_GUIDE("完成指引"),
    /** 冰鲜连续天数检查 */
    STREAK_CHECK("冰鲜检查"),
    /** 保鲜评分计算 */
    FRESHNESS_SCORE("保鲜评分"),
    /** 整理冰箱 */
    ORGANIZE("整理冰箱"),
    /** 创建采购方案 */
    CREATE_PURCHASE_PLAN("创建采购方案"),
    /** 完成采购方案 */
    COMPLETE_PURCHASE_PLAN("完成采购方案"),
    /** 批量入库 */
    BATCH_ADD_ITEM("批量入库");

    /**
     * 触发行为描述。
     */
    private final String desc;
}
