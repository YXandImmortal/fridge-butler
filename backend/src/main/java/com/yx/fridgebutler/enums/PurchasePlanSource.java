package com.yx.fridgebutler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采购计划来源枚举。
 * <p>记录采购计划是通过何种方式创建的，用于 AI 调度追溯、游戏化统计和数据分析。</p>
 */
@Getter
@AllArgsConstructor
public enum PurchasePlanSource {

    /** 通过日常 AI 推荐创建。 */
    DAILY_RECOMMEND("DAILY_RECOMMEND", "日常 AI 推荐"),

    /** 通过特殊场景 AI 生成创建。 */
    SPECIAL_GENERATE("SPECIAL_GENERATE", "特殊场景 AI 生成"),

    /** 用户手动创建。 */
    MANUAL_CREATE("MANUAL_CREATE", "手动创建"),

    /** 通过用户采购计划模板创建。 */
    TEMPLATE("TEMPLATE", "模板创建");

    private final String code;
    private final String desc;

    /**
     * 根据编码查找枚举。
     *
     * @param code 编码
     * @return 枚举值，找不到返回 MANUAL_CREATE
     */
    public static PurchasePlanSource fromCode(String code) {
        for (PurchasePlanSource source : values()) {
            if (source.getCode().equalsIgnoreCase(code)) {
                return source;
            }
        }
        return MANUAL_CREATE;
    }
}
