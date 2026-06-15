package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户冰鲜连续天数数据访问层。
 * <p>提供用户冰鲜连续天数实体的增删改查操作。</p>
 */
@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

    /**
     * 根据用户ID查询冰鲜连续天数记录。
     *
     * @param userId 用户ID
     * @return 冰鲜连续天数记录
     */
    Optional<UserStreak> findByUserId(Long userId);
}
