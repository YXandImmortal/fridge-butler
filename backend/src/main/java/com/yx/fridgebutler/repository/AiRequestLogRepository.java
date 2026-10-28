package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.AiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {

    @Query(value = """
            SELECT COUNT(*) AS total_count,
                   COALESCE(SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS success_count,
                   COALESCE(SUM(CASE WHEN status = 'FAILURE' THEN 1 ELSE 0 END), 0) AS failure_count
            FROM ai_request_log
            """, nativeQuery = true)
    List<Object[]> summarizeAll();

    @Query(value = """
            SELECT COUNT(*) AS total_count,
                   COALESCE(SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS success_count,
                   COALESCE(SUM(CASE WHEN status = 'FAILURE' THEN 1 ELSE 0 END), 0) AS failure_count
            FROM ai_request_log
            WHERE create_time >= :startTime
            """, nativeQuery = true)
    List<Object[]> summarizeSince(@Param("startTime") Instant startTime);

    @Query(value = """
            SELECT DATE(CONVERT_TZ(create_time, '+00:00', '+08:00')) AS request_date,
                   COUNT(*) AS total_count,
                   COALESCE(SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS success_count,
                   COALESCE(SUM(CASE WHEN status = 'FAILURE' THEN 1 ELSE 0 END), 0) AS failure_count
            FROM ai_request_log
            WHERE create_time >= :startTime
            GROUP BY DATE(CONVERT_TZ(create_time, '+00:00', '+08:00'))
            ORDER BY request_date
            """, nativeQuery = true)
    List<Object[]> findTrendSince(@Param("startTime") Instant startTime);
}
