package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.UserExpLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * 用户经验值变动日志数据访问层。
 * <p>提供用户经验值日志的查询和保存操作。</p>
 */
@Repository
public interface UserExpLogRepository extends JpaRepository<UserExpLog, Long> {

    /**
     * 根据用户ID分页查询经验值日志，按创建时间降序排列。
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 经验值日志分页结果
     */
    Page<UserExpLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 统计用户在指定时间范围内某行为类型的次数。
     *
     * @param userId     用户ID
     * @param actionType 行为类型
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 次数
     */
    long countByUserIdAndActionTypeAndCreatedAtBetween(Long userId, String actionType, Instant startTime, Instant endTime);

    /**
     * 统计用户某行为类型的总次数（不限时间）。
     *
     * @param userId     用户ID
     * @param actionType 行为类型
     * @return 总次数
     */
    long countByUserIdAndActionType(Long userId, String actionType);
}
