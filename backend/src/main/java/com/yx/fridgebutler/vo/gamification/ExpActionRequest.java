package com.yx.fridgebutler.vo.gamification;

import com.yx.fridgebutler.enums.ExpActionType;

/**
 * 经验值行为请求记录。
 * <p>用于统一结算服务描述一次需要发放经验值的业务行为。</p>
 *
 * @param actionType 行为类型
 * @param customExp  自定义经验值（null 则使用默认值）
 * @param relatedId  关联业务ID
 * @param actionDesc 行为描述
 */
public record ExpActionRequest(
        ExpActionType actionType,
        Integer customExp,
        Long relatedId,
        String actionDesc
) {

    public ExpActionRequest(ExpActionType actionType) {
        this(actionType, null, null, null);
    }

    public ExpActionRequest(ExpActionType actionType, Integer customExp) {
        this(actionType, customExp, null, null);
    }
}
