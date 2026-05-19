package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizFridgeItem;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 冰箱物品数据访问层。
 * <p>提供冰箱物品实体的增删改查操作，以及按冰箱ID、关键字、分类、单位等条件查询的自定义方法。</p>
 */
@Repository
public interface BizFridgeItemRepository extends JpaRepository<BizFridgeItem, Long> {

    /**
     * 根据冰箱ID查询未删除的物品列表。
     *
     * @param fridgeId 冰箱ID
     * @return 该冰箱下未删除的物品列表
     */
    List<BizFridgeItem> findByFridgeIdAndIsDeletedFalse(Long fridgeId);

    /**
     * 根据冰箱ID统计未删除的物品数量。
     *
     * @param fridgeId 冰箱ID
     * @return 该冰箱下未删除的物品数量
     */
    long countByFridgeIdAndIsDeletedFalse(Long fridgeId);

    /**
     * 根据多个冰箱ID及多种条件搜索物品。
     * <p>支持按关键字（物品名称模糊匹配）、分类ID、单位ID、单位类型ID进行筛选。</p>
     *
     * @param fridgeIds  冰箱ID列表
     * @param keyword    搜索关键字（物品名称），可为空
     * @param categoryId 分类ID，可为空
     * @param unitId     单位ID，可为空
     * @param unitTypeId 单位类型ID，可为空
     * @param sort       排序规则
     * @return 符合条件的物品列表
     */
    @Query("""
            SELECT i FROM BizFridgeItem i
            LEFT JOIN BizItemUnit u ON i.itemUnitId = u.id
            WHERE i.fridgeId IN :fridgeIds
              AND i.isDeleted = false
              AND (:keyword IS NULL OR :keyword = ''
                   OR i.itemName LIKE :keyword)
              AND (:categoryId IS NULL OR i.categoryId = :categoryId)
              AND (:unitId IS NULL OR i.itemUnitId = :unitId)
              AND (:unitTypeId IS NULL OR u.unitTypeId = :unitTypeId)
            """)
    List<BizFridgeItem> searchItems(
            @Param("fridgeIds") List<Long> fridgeIds,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("unitId") Long unitId,
            @Param("unitTypeId") Long unitTypeId,
            Sort sort
    );

    /**
     * 根据单个冰箱ID及多种条件搜索物品。
     * <p>支持按关键字（物品名称模糊匹配）、分类ID、单位ID、单位类型ID进行筛选。</p>
     *
     * @param fridgeId   冰箱ID
     * @param keyword    搜索关键字（物品名称），可为空
     * @param categoryId 分类ID，可为空
     * @param unitId     单位ID，可为空
     * @param unitTypeId 单位类型ID，可为空
     * @param sort       排序规则
     * @return 符合条件的物品列表
     */
    @Query("""
            SELECT i FROM BizFridgeItem i
            LEFT JOIN BizItemUnit u ON i.itemUnitId = u.id
            WHERE i.fridgeId = :fridgeId
              AND i.isDeleted = false
              AND (:keyword IS NULL OR :keyword = ''
                   OR i.itemName LIKE :keyword)
              AND (:categoryId IS NULL OR i.categoryId = :categoryId)
              AND (:unitId IS NULL OR i.itemUnitId = :unitId)
              AND (:unitTypeId IS NULL OR u.unitTypeId = :unitTypeId)
            """)
    List<BizFridgeItem> searchItemsByFridgeId(
            @Param("fridgeId") Long fridgeId,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("unitId") Long unitId,
            @Param("unitTypeId") Long unitTypeId,
            Sort sort
    );

    /**
     * 根据分类ID统计未删除的物品数量。
     *
     * @param categoryId 分类ID
     * @return 该分类下未删除的物品数量
     */
    long countByCategoryIdAndIsDeletedFalse(Long categoryId);

    /**
     * 根据多个冰箱ID查询可能临期的候选物品。
     * <p>过滤条件：未删除、有生产日期、有保质期天数、保质期≤30天（长保质期物品不参与临期计算）。</p>
     *
     * @param fridgeIds 冰箱ID列表
     * @return 符合条件的物品列表
     */
    @Query("""
            SELECT i FROM BizFridgeItem i
            WHERE i.fridgeId IN :fridgeIds
              AND i.isDeleted = false
              AND i.productionDate IS NOT NULL
              AND i.shelfLifeDays IS NOT NULL
              AND i.shelfLifeDays <= 30
            """)
    List<BizFridgeItem> findExpiringCandidates(@Param("fridgeIds") List<Long> fridgeIds);
}
