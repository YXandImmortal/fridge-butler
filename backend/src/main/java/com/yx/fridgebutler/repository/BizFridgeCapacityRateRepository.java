package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizFridgeCapacityRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 冰箱容量利用率缓存数据访问层。
 * <p>提供冰箱容量利用率缓存实体的增删改查操作，以及按冰箱ID查询的方法。</p>
 */
@Repository
public interface BizFridgeCapacityRateRepository extends JpaRepository<BizFridgeCapacityRate, Long> {

    /**
     * 根据冰箱ID查询未删除的容量利用率缓存。
     *
     * @param fridgeId 冰箱ID
     * @return 容量利用率缓存Optional对象
     */
    @Query("SELECT c FROM BizFridgeCapacityRate c WHERE c.fridgeId = :fridgeId AND c.isDeleted = 0")
    Optional<BizFridgeCapacityRate> findByFridgeId(@Param("fridgeId") Long fridgeId);

    /**
     * 根据多个冰箱ID查询未删除的容量利用率缓存列表。
     *
     * @param fridgeIds 冰箱ID列表
     * @return 容量利用率缓存列表
     */
    @Query("SELECT c FROM BizFridgeCapacityRate c WHERE c.fridgeId IN :fridgeIds AND c.isDeleted = 0")
    List<BizFridgeCapacityRate> findByFridgeIdIn(@Param("fridgeIds") List<Long> fridgeIds);
}
