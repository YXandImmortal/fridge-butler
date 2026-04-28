package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BizItemCategoryRepository extends JpaRepository<BizItemCategory, Long> {

    @Query("SELECT c FROM BizItemCategory c WHERE c.isDeleted = false AND (c.isSystemDefault = true OR c.ownerId = :ownerId)")
    List<BizItemCategory> findAllByOwnerIdOrSystemDefault(@Param("ownerId") Long ownerId);
}
