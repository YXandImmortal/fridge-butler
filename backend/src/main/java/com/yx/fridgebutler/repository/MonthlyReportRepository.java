package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 月度报告数据访问层。
 */
@Repository
public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    /**
     * 根据用户ID和年月查询月度报告。
     *
     * @param userId    用户ID
     * @param yearMonth 年月，如 2026-05
     * @return 月度报告
     */
    Optional<MonthlyReport> findByUserIdAndYearMonth(Long userId, String yearMonth);

    /**
     * 检查用户指定年月是否已有报告。
     *
     * @param userId    用户ID
     * @param yearMonth 年月
     * @return true 表示已存在
     */
    boolean existsByUserIdAndYearMonth(Long userId, String yearMonth);
}
