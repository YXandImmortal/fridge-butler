package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemChangeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * 物品变更记录数据访问层。
 * <p>提供物品变更记录实体的增删改查操作，继承 JpaRepository 使用基础 CRUD 方法。</p>
 */
@Repository
public interface BizItemChangeRecordRepository extends JpaRepository<BizItemChangeRecord, Long> {

    /**
     * 检查指定冰箱在指定时间之后是否有指定类型的变更记录。
     *
     * @param fridgeId   冰箱ID
     * @param createTime 时间阈值
     * @param changeType 变更类型，如 UPDATE_NUM
     * @return true 表示有符合条件的变更记录
     */
    boolean existsByFridgeIdAndCreateTimeAfterAndChangeType(Long fridgeId, Instant createTime, String changeType);

    /**
     * 统计指定用户在指定时间范围内的变更记录数量。
     *
     * @param operatorId 操作人ID
     * @param startTime  起始时间
     * @param endTime    结束时间
     * @return 变更记录数量
     */
    long countByOperatorIdAndCreateTimeBetween(Long operatorId, Instant startTime, Instant endTime);
}
