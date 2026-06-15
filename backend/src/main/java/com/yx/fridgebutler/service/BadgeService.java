package com.yx.fridgebutler.service;

import com.yx.fridgebutler.enums.BadgeCode;
import com.yx.fridgebutler.enums.BadgeTriggerType;
import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.BadgeVO;

import java.util.List;

/**
 * 徽章服务接口。
 * <p>定义徽章解锁判定、用户徽章查询、行为计数等核心逻辑。</p>
 */
public interface BadgeService {

    /**
     * 检查并解锁徽章。
     * <p>根据触发类型执行对应的计数和条件检查，若满足解锁条件则自动解锁徽章并发放 EXP。</p>
     *
     * @param userId      用户ID
     * @param triggerType 触发类型
     * @param context     上下文数据（如物品实体、连续天数、评分等，可为 null）
     */
    void checkAndUnlock(Long userId, BadgeTriggerType triggerType, Object context);

    /**
     * 检查并解锁徽章，并返回本次新解锁的徽章列表。
     * <p>根据触发类型执行对应的计数和条件检查，若满足解锁条件则自动解锁徽章并发放 EXP。</p>
     *
     * @param userId      用户ID
     * @param triggerType 触发类型
     * @param context     上下文数据（如物品实体、连续天数、评分等，可为 null）
     * @return 本次新解锁的徽章列表（无新增时返回空列表）
     */
    List<BadgeUnlockInfo> checkAndUnlockWithResult(Long userId, BadgeTriggerType triggerType, Object context);

    /**
     * 获取用户的所有徽章（含已解锁和未解锁状态）。
     *
     * @param userId 用户ID
     * @return 徽章列表
     */
    List<BadgeVO> getUserBadges(Long userId);

    /**
     * 检查用户是否已解锁指定徽章。
     *
     * @param userId    用户ID
     * @param badgeCode 徽章编码
     * @return true 表示已解锁
     */
    boolean hasBadge(Long userId, BadgeCode badgeCode);

    /**
     * 手动解锁指定徽章（用于定时任务等特殊场景）。
     *
     * @param userId    用户ID
     * @param badgeCode 徽章编码
     */
    void unlockBadge(Long userId, BadgeCode badgeCode);

    /**
     * 手动解锁指定徽章，并可控制是否发送系统通知。
     * <p>用于历史徽章补发等场景。</p>
     *
     * @param userId           用户ID
     * @param badgeCode        徽章编码
     * @param sendNotification 是否发送系统通知
     */
    void unlockBadge(Long userId, BadgeCode badgeCode, boolean sendNotification);
}
