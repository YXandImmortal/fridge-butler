package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizUnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 单位类型数据访问层。
 * <p>提供单位类型实体的增删改查操作，以及按所有者查询系统默认类型和自定义类型的方法。</p>
 */
@Repository
public interface BizUnitTypeRepository extends JpaRepository<BizUnitType, Long> {

    /**
     * 查询指定用户可用的单位类型列表，包括系统默认类型和该用户的自定义类型。
     *
     * @param ownerId 所有者ID
     * @return 未删除的系统默认类型或属于该用户的类型列表
     */
    @Query("SELECT t FROM BizUnitType t WHERE t.isDeleted = false AND (t.isSystemDefault = true OR t.ownerId = :ownerId)")
    List<BizUnitType> findAllByOwnerIdOrSystemDefault(@Param("ownerId") Long ownerId);
}
