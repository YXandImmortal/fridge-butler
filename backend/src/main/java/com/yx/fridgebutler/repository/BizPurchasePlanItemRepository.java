package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizPurchasePlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 采购方案物品清单数据访问层。
 */
@Repository
public interface BizPurchasePlanItemRepository extends JpaRepository<BizPurchasePlanItem, Long> {

    /**
     * 根据方案ID查询所有物品。
     */
    List<BizPurchasePlanItem> findByPlanId(Long planId);

    /**
     * 根据方案ID和状态查询物品。
     */
    List<BizPurchasePlanItem> findByPlanIdAndStatus(Long planId, Byte status);
}
