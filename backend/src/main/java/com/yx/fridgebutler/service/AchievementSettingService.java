package com.yx.fridgebutler.service;

import com.yx.fridgebutler.entity.UserAchievementSetting;
import com.yx.fridgebutler.vo.gamification.AchievementSettingsVO;

/**
 * 成就设置服务接口。
 * <p>定义用户成就系统个性化设置的查询和更新逻辑。</p>
 */
public interface AchievementSettingService {

    /**
     * 获取或创建用户成就设置记录。
     *
     * @param userId 用户ID
     * @return 成就设置记录
     */
    UserAchievementSetting getOrCreateSetting(Long userId);

    /**
     * 获取用户成就设置 VO。
     *
     * @param userId 用户ID
     * @return 成就设置 VO
     */
    AchievementSettingsVO getSettingVO(Long userId);

    /**
     * 更新用户成就设置。
     *
     * @param userId  用户ID
     * @param request 设置请求
     */
    void updateSetting(Long userId, AchievementSettingsVO request);
}
