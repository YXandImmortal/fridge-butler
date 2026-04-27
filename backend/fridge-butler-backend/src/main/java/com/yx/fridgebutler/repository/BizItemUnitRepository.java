package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BizItemUnitRepository extends JpaRepository<BizItemUnit, Long> {
}
