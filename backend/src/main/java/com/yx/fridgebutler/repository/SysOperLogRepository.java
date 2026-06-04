package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.SysOperLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * 操作日志数据访问层。
 * <p>提供操作日志实体的增删改查及分页筛选查询。</p>
 */
@Repository
public interface SysOperLogRepository extends JpaRepository<SysOperLog, Long> {

    /**
     * 分页查询操作日志（支持多条件筛选）。
     *
     * @param keyword     关键词（模糊匹配用户名或URI）
     * @param method      请求方法
     * @param statusStart 状态码范围起始
     * @param statusEnd   状态码范围结束
     * @param startTime   起始时间
     * @param endTime     结束时间
     * @param pageable    分页参数
     * @return 日志分页数据
     */
    @Query("""
            SELECT l FROM SysOperLog l
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR l.username LIKE %:keyword%
                   OR l.uri LIKE %:keyword%)
              AND (:method IS NULL OR :method = '' OR l.method = :method)
              AND (:statusStart IS NULL OR l.statusCode >= :statusStart)
              AND (:statusEnd IS NULL OR l.statusCode <= :statusEnd)
              AND (:startTime IS NULL OR l.createTime >= :startTime)
              AND (:endTime IS NULL OR l.createTime <= :endTime)
            ORDER BY l.createTime DESC
            """)
    Page<SysOperLog> findByConditions(@Param("keyword") String keyword,
                                      @Param("method") String method,
                                      @Param("statusStart") Integer statusStart,
                                      @Param("statusEnd") Integer statusEnd,
                                      @Param("startTime") Instant startTime,
                                      @Param("endTime") Instant endTime,
                                      Pageable pageable);
}
