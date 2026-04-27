package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BizItemCategoryRepository extends JpaRepository<BizItemCategory, Long> {
}
