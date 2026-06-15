package com.yx.fridgebutler.vo.gamification;

import com.yx.fridgebutler.enums.BadgeTriggerType;

/**
 * 徽章触发请求记录。
 * <p>用于统一结算服务描述一次需要检查的徽章触发条件。</p>
 *
 * @param triggerType 徽章触发类型
 * @param context     上下文数据（可为 null）
 */
public record BadgeTriggerRequest(
        BadgeTriggerType triggerType,
        Object context
) {

    public BadgeTriggerRequest(BadgeTriggerType triggerType) {
        this(triggerType, null);
    }
}
