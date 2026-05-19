package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemTakeOutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * 物品取出记录数据访问层。
 * <p>提供物品取出记录实体的增删改查操作，继承 JpaRepository 使用基础 CRUD 方法。</p>
 */
@Repository
public interface BizItemTakeOutRecordRepository extends JpaRepository<BizItemTakeOutRecord, Long> {

    /**
     * 查询指定用户在指定时间范围内的每日取出次数。
     * <p>按日期分组统计，返回近30天内有取出记录的日期及对应次数。</p>
     *
     * @param operatorId 操作人ID（当前用户ID）
     * @param fridgeId   冰箱ID（为null时统计所有冰箱）
     * @param startTime  起始时间（30天前）
     * @return 每日统计列表，每个元素为 [date, count]，date格式为yyyy-MM-dd
     */
    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count " +
            "FROM biz_item_take_out_record " +
            "WHERE operator_id = :operatorId " +
            "AND create_time >= :startTime " +
            "AND (:fridgeId IS NULL OR fridge_id = :fridgeId) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY DATE(create_time)",
            nativeQuery = true)
    List<Object[]> countDailyByOperatorIdAndTimeRange(
            @Param("operatorId") Long operatorId,
            @Param("fridgeId") Long fridgeId,
            @Param("startTime") Instant startTime);

    /**
     * 检查指定冰箱在指定时间之后是否有取出物品记录。
     *
     * @param fridgeId   冰箱ID
     * @param createTime 时间阈值
     * @return true 表示有取出记录
     */
    boolean existsByFridgeIdAndCreateTimeAfter(Long fridgeId, Instant createTime);
}
