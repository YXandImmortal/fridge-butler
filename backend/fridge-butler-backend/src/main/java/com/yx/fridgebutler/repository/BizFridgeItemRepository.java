package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizFridgeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BizFridgeItemRepository extends JpaRepository<BizFridgeItem, Long> {

    List<BizFridgeItem> findByFridgeIdAndIsDeletedFalse(Long fridgeId);

    long countByFridgeIdAndIsDeletedFalse(Long fridgeId);
}
