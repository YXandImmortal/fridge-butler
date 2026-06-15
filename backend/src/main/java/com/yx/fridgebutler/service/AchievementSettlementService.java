package com.yx.fridgebutler.service;

import com.yx.fridgebutler.enums.BadgeTriggerType;
import com.yx.fridgebutler.enums.ExpActionType;
import com.yx.fridgebutler.vo.gamification.AchievementSettlementResult;
import com.yx.fridgebutler.vo.gamification.BadgeTriggerRequest;
import com.yx.fridgebutler.vo.gamification.ExpActionRequest;

import java.util.List;

/**
 * 成就统一结算服务。
 * <p>将“直接 EXP 发放”与“徽章解锁”合并为一个原子结算单元，确保返回的
 * {@code leveledUp} 反映两者共同作用后的真实等级变化。</p>
 */
public interface AchievementSettlementService {

    /**
     * 仅单行为（无徽章触发）。
     *
     * @param userId     用户ID
     * @param actionType 直接经验行为类型
     * @return 结算结果
     */
    AchievementSettlementResult settle(Long userId, ExpActionType actionType);

    /**
     * 单行为 + 单徽章触发。
     *
     * @param userId        用户ID
     * @param actionType    直接经验行为类型
     * @param badgeTrigger  徽章触发类型
     * @param badgeContext  徽章上下文
     * @return 结算结果
     */
    AchievementSettlementResult settle(
            Long userId,
            ExpActionType actionType,
            BadgeTriggerType badgeTrigger,
            Object badgeContext);

    /**
     * 单行为 + 多徽章触发。
     *
     * @param userId         用户ID
     * @param actionType     直接经验行为类型
     * @param badgeTriggers  徽章触发请求列表
     * @return 结算结果
     */
    AchievementSettlementResult settle(
            Long userId,
            ExpActionType actionType,
            List<BadgeTriggerRequest> badgeTriggers);

    /**
     * 多行为 + 多徽章触发。
     * <p>适用于一次业务操作可能触发多个经验行为（如取出临期食材 + 整理奖励）。</p>
     *
     * @param userId         用户ID
     * @param actions        经验行为请求列表
     * @param badgeTriggers  徽章触发请求列表
     * @return 结算结果
     */
    AchievementSettlementResult settle(
            Long userId,
            List<ExpActionRequest> actions,
            List<BadgeTriggerRequest> badgeTriggers);

    /**
     * 仅徽章触发（无直接经验行为）。
     * <p>适用于创建冰箱等本身不发 EXP，但可能因徽章解锁而发 EXP 的场景。</p>
     *
     * @param userId         用户ID
     * @param badgeTriggers  徽章触发请求列表
     * @return 结算结果
     */
    AchievementSettlementResult settleBadgesOnly(
            Long userId,
            List<BadgeTriggerRequest> badgeTriggers);
}
