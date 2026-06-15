package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.UserAchievementSetting;
import com.yx.fridgebutler.repository.UserAchievementSettingRepository;
import com.yx.fridgebutler.service.AchievementSettingService;
import com.yx.fridgebutler.vo.gamification.AchievementSettingsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 成就设置服务实现类。
 * <p>处理用户成就系统个性化设置的查询和更新逻辑。</p>
 */
@Slf4j
@Service
public class AchievementSettingServiceImpl implements AchievementSettingService {

    @Autowired
    private UserAchievementSettingRepository settingRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAchievementSetting getOrCreateSetting(Long userId) {
        return settingRepository.findByUserId(userId).orElseGet(() -> {
            UserAchievementSetting setting = new UserAchievementSetting();
            setting.setUserId(userId);
            setting.setPanelHidden((byte) 0);
            setting.setAutoStreakProtect((byte) 1);
            setting.setStreakProtectNotify((byte) 1);
            setting.setUpdatedAt(Instant.now());
            return settingRepository.save(setting);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AchievementSettingsVO getSettingVO(Long userId) {
        UserAchievementSetting setting = getOrCreateSetting(userId);
        return AchievementSettingsVO.builder()
                .panelHidden(setting.getPanelHidden() != null && setting.getPanelHidden() == 1)
                .autoStreakProtect(setting.getAutoStreakProtect() != null && setting.getAutoStreakProtect() == 1)
                .streakProtectNotify(setting.getStreakProtectNotify() != null && setting.getStreakProtectNotify() == 1)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateSetting(Long userId, AchievementSettingsVO request) {
        UserAchievementSetting setting = getOrCreateSetting(userId);
        if (request.getPanelHidden() != null) {
            setting.setPanelHidden(request.getPanelHidden() ? (byte) 1 : (byte) 0);
        }
        if (request.getAutoStreakProtect() != null) {
            setting.setAutoStreakProtect(request.getAutoStreakProtect() ? (byte) 1 : (byte) 0);
        }
        if (request.getStreakProtectNotify() != null) {
            setting.setStreakProtectNotify(request.getStreakProtectNotify() ? (byte) 1 : (byte) 0);
        }
        setting.setUpdatedAt(Instant.now());
        settingRepository.save(setting);
        log.info("用户{}成就设置更新成功", userId);
    }
}
