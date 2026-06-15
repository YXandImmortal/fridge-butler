package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.UserAchievementSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户成就设置数据访问层。
 * <p>提供用户成就个性化设置实体的增删改查操作。</p>
 */
@Repository
public interface UserAchievementSettingRepository extends JpaRepository<UserAchievementSetting, Long> {

    /**
     * 根据用户ID查询成就设置记录。
     *
     * @param userId 用户ID
     * @return 成就设置记录
     */
    Optional<UserAchievementSetting> findByUserId(Long userId);
}
