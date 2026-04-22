package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizFridge;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BizFridgeRepository extends JpaRepository<BizFridge, Long> {

    List<BizFridge> findByOwnerIdAndIsDeletedFalse(Long ownerId, Sort sort);

    Optional<BizFridge> findByIdAndOwnerIdAndIsDeletedFalse(Long id, Long ownerId);

    boolean existsByFridgeNameAndOwnerIdAndIsDeletedFalse(String fridgeName, Long ownerId);

    @Modifying
    @Query("UPDATE BizFridge f SET f.isDefault = false WHERE f.ownerId = ?1 AND f.isDeleted = false AND f.isDefault = true")
    void unsetDefaultByOwnerId(Long ownerId);

    @Query("""
            SELECT f FROM BizFridge f
            WHERE f.ownerId = :ownerId
              AND f.isDeleted = false
              AND (:keyword IS NULL OR :keyword = ''
                   OR f.fridgeName LIKE :keyword
                   OR f.fridgeAddress LIKE :keyword
                   OR f.remark LIKE :keyword)
            ORDER BY
                CASE WHEN f.fridgeName LIKE :keyword THEN 3 ELSE 0 END DESC,
                CASE WHEN f.fridgeAddress LIKE :keyword THEN 2 ELSE 0 END DESC,
                CASE WHEN f.remark LIKE :keyword THEN 1 ELSE 0 END DESC,
                f.createTime DESC
            """)
    List<BizFridge> searchByKeyword(@Param("ownerId") Long ownerId, @Param("keyword") String keyword);
}
