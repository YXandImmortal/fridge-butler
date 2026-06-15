package com.yx.fridgebutler.service;

import com.yx.fridgebutler.entity.UserStreak;
import com.yx.fridgebutler.vo.gamification.StreakInfoVO;

/**
 * 冰鲜连续天数服务接口。
 * <p>定义冰鲜连续天数管理、保护机制、月度重置等逻辑。</p>
 */
public interface StreakService {

    /**
     * 获取或创建用户冰鲜连续天数记录。
     *
     * @param userId 用户ID
     * @return 冰鲜连续天数记录
     */
    UserStreak getOrCreateUserStreak(Long userId);

    /**
     * 检查并重置月度保护次数（每月 1 号执行）。
     *
     * @param streak 冰鲜连续天数记录
     */
    void resetMonthlyProtectIfNeeded(UserStreak streak);

    /**
     * 使用一次保护次数。
     *
     * @param userId 用户ID
     * @return 是否成功使用
     */
    boolean useProtect(Long userId);

    /**
     * 增加连续天数（无过期时调用）。
     *
     * @param userId 用户ID
     */
    void incrementStreak(Long userId);

    /**
     * 中断连续天数（有过期且未使用保护时调用）。
     *
     * @param userId 用户ID
     */
    void breakStreak(Long userId);

    /**
     * 获取冰鲜连续天数信息 VO。
     *
     * @param userId 用户ID
     * @return 冰鲜连续天数信息
     */
    StreakInfoVO getStreakInfo(Long userId);
}
