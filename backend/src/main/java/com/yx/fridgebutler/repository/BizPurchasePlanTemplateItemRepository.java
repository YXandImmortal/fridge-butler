package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizPurchasePlanTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户采购计划模板物品清单数据访问层。
 */
@Repository
public interface BizPurchasePlanTemplateItemRepository extends JpaRepository<BizPurchasePlanTemplateItem, Long> {

    /**
     * 根据模板ID查询所有物品。
     */
    List<BizPurchasePlanTemplateItem> findByTemplateId(Long templateId);
}
