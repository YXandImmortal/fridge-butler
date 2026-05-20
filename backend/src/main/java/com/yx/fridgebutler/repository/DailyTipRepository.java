package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.DailyTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 每日小贴士数据访问层。
 */
@Repository
public interface DailyTipRepository extends JpaRepository<DailyTip, Long> {

    /**
     * 根据日期查询每日小贴士。
     *
     * @param tipDate 日期
     * @return 每日小贴士
     */
    Optional<DailyTip> findByTipDate(LocalDate tipDate);

    /**
     * 判断指定日期是否已存在小贴士。
     *
     * @param tipDate 日期
     * @return 是否存在
     */
    boolean existsByTipDate(LocalDate tipDate);
}
