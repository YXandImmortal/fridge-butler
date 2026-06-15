package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户徽章仓库接口。
 */
@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    /**
     * 根据用户ID和徽章编码查询徽章记录。
     *
     * @param userId   用户ID
     * @param badgeCode 徽章编码
     * @return 徽章记录
     */
    Optional<UserBadge> findByUserIdAndBadgeCode(Long userId, String badgeCode);

    /**
     * 查询用户已解锁的所有徽章。
     *
     * @param userId 用户ID
     * @return 徽章列表
     */
    List<UserBadge> findByUserId(Long userId);

    /**
     * 检查用户是否已解锁指定徽章。
     *
     * @param userId   用户ID
     * @param badgeCode 徽章编码
     * @return true 表示已解锁
     */
    boolean existsByUserIdAndBadgeCode(Long userId, String badgeCode);
}
