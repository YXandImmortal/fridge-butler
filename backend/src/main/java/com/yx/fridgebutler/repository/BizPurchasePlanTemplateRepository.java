package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizPurchasePlanTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户采购计划模板数据访问层。
 */
@Repository
public interface BizPurchasePlanTemplateRepository extends JpaRepository<BizPurchasePlanTemplate, Long> {

    /**
     * 根据ID和用户ID查询模板。
     */
    Optional<BizPurchasePlanTemplate> findByIdAndUserId(Long id, Long userId);

    /**
     * 查询用户的所有模板。
     */
    List<BizPurchasePlanTemplate> findByUserId(Long userId, Sort sort);

    /**
     * 统计用户的模板数量。
     */
    long countByUserId(Long userId);

    /**
     * 检查同一用户下是否存在指定名称的模板。
     */
    boolean existsByUserIdAndTemplateName(Long userId, String templateName);
}
