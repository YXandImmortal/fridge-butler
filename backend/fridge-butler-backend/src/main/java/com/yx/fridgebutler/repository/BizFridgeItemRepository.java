package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizFridgeItem;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BizFridgeItemRepository extends JpaRepository<BizFridgeItem, Long> {

    List<BizFridgeItem> findByFridgeIdAndIsDeletedFalse(Long fridgeId);

    long countByFridgeIdAndIsDeletedFalse(Long fridgeId);

    @Query("""
            SELECT i FROM BizFridgeItem i
            WHERE i.fridgeId IN :fridgeIds
              AND i.isDeleted = false
              AND (:keyword IS NULL OR :keyword = ''
                   OR i.itemName LIKE :keyword)
              AND (:categoryId IS NULL OR i.categoryId = :categoryId)
              AND (:unitTypeId IS NULL OR i.itemUnitId IN (
                  SELECT u.id FROM BizItemUnit u WHERE u.unitTypeId = :unitTypeId AND u.isDeleted = false
              ))
            """)
    List<BizFridgeItem> searchItems(
            @Param("fridgeIds") List<Long> fridgeIds,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("unitTypeId") Long unitTypeId,
            Sort sort
    );

    @Query("""
            SELECT i FROM BizFridgeItem i
            WHERE i.fridgeId = :fridgeId
              AND i.isDeleted = false
              AND (:keyword IS NULL OR :keyword = ''
                   OR i.itemName LIKE :keyword)
              AND (:categoryId IS NULL OR i.categoryId = :categoryId)
              AND (:unitTypeId IS NULL OR i.itemUnitId IN (
                  SELECT u.id FROM BizItemUnit u WHERE u.unitTypeId = :unitTypeId AND u.isDeleted = false
              ))
            """)
    List<BizFridgeItem> searchItemsByFridgeId(
            @Param("fridgeId") Long fridgeId,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("unitTypeId") Long unitTypeId,
            Sort sort
    );
}
