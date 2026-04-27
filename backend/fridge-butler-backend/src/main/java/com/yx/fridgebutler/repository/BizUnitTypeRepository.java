package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizUnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BizUnitTypeRepository extends JpaRepository<BizUnitType, Long> {
}
