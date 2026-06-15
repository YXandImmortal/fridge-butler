package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.DailyFreshnessSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 每日保鲜评分快照数据访问层。
 */
@Repository
public interface DailyFreshnessSnapshotRepository extends JpaRepository<DailyFreshnessSnapshot, Long> {

    /**
     * 根据用户ID和快照日期查询快照。
     *
     * @param userId      用户ID
     * @param snapshotDate 快照日期
     * @return 快照记录
     */
    Optional<DailyFreshnessSnapshot> findByUserIdAndSnapshotDate(Long userId, LocalDate snapshotDate);

    /**
     * 查询用户在指定日期范围内的快照列表。
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 快照列表
     */
    List<DailyFreshnessSnapshot> findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 检查用户指定日期是否已有快照。
     *
     * @param userId      用户ID
     * @param snapshotDate 快照日期
     * @return true 表示已存在
     */
    boolean existsByUserIdAndSnapshotDate(Long userId, LocalDate snapshotDate);

    /**
     * 查询用户历史最高评分快照。
     *
     * @param userId 用户ID
     * @return 最高评分快照
     */
    Optional<DailyFreshnessSnapshot> findTopByUserIdOrderByFreshnessScoreDesc(Long userId);

    /**
     * 查询用户指定等级历史快照数量。
     *
     * @param userId    用户ID
     * @param scoreGrade 评分等级
     * @return 快照数量
     */
    long countByUserIdAndScoreGrade(Long userId, String scoreGrade);
}
