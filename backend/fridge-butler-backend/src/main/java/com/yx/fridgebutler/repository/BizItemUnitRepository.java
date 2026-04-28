package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BizItemUnitRepository extends JpaRepository<BizItemUnit, Long> {

    @Query("SELECT u FROM BizItemUnit u WHERE u.isDeleted = false AND (u.isSystemDefault = true OR u.ownerId = :ownerId)")
    List<BizItemUnit> findAllByOwnerIdOrSystemDefault(@Param("ownerId") Long ownerId);
}
