package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.UserActionCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 用户行为计数器仓库接口。
 */
@Repository
public interface UserActionCounterRepository extends JpaRepository<UserActionCounter, Long> {

    /**
     * 根据用户ID、计数类型和日期查询计数器记录。
     *
     * @param userId      用户ID
     * @param counterType 计数类型
     * @param countDate   计数日期
     * @return 计数器记录
     */
    Optional<UserActionCounter> findByUserIdAndCounterTypeAndCountDate(Long userId, String counterType, LocalDate countDate);

    /**
     * 统计用户指定计数类型的累计记录数（用于 ORGANIZE_DAY 等按天统计的徽章）。
     *
     * @param userId      用户ID
     * @param counterType 计数类型
     * @return 记录数量
     */
    long countByUserIdAndCounterType(Long userId, String counterType);

    /**
     * 根据用户ID和计数类型查询计数器记录（用于全局累计计数，如 NIGHT_OWL）。
     *
     * @param userId      用户ID
     * @param counterType 计数类型
     * @return 计数器记录
     */
    Optional<UserActionCounter> findByUserIdAndCounterTypeAndCountDateIsNull(Long userId, String counterType);
}
