package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizUnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BizUnitTypeRepository extends JpaRepository<BizUnitType, Long> {

    @Query("SELECT t FROM BizUnitType t WHERE t.isDeleted = false AND (t.isSystemDefault = true OR t.ownerId = :ownerId)")
    List<BizUnitType> findAllByOwnerIdOrSystemDefault(@Param("ownerId") Long ownerId);
}
