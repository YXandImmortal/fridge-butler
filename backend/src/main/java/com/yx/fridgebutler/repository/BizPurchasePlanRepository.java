package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizPurchasePlan;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 采购方案数据访问层。
 */
@Repository
public interface BizPurchasePlanRepository extends JpaRepository<BizPurchasePlan, Long> {

    /**
     * 根据ID和用户ID查询方案。
     */
    Optional<BizPurchasePlan> findByIdAndUserId(Long id, Long userId);

    /**
     * 查询用户的所有方案。
     */
    List<BizPurchasePlan> findByUserId(Long userId, Sort sort);

    /**
     * 根据状态查询用户的方案。
     */
    List<BizPurchasePlan> findByUserIdAndPlanStatus(Long userId, Byte planStatus, Sort sort);

    /**
     * 根据来源查询用户的方案。
     */
    List<BizPurchasePlan> findByUserIdAndSource(Long userId, String source, Sort sort);

    /**
     * 统计用户的方案数量。
     */
    long countByUserId(Long userId);
}
