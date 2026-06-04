package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 物品单位数据访问层。
 * <p>提供物品单位实体的增删改查操作，以及按所有者查询系统默认单位和自定义单位的方法。</p>
 */
@Repository
public interface BizItemUnitRepository extends JpaRepository<BizItemUnit, Long> {

    /**
     * 查询指定用户可用的单位列表，包括系统默认单位和该用户的自定义单位。
     *
     * @param ownerId 所有者ID
     * @return 未删除的系统默认单位或属于该用户的单位列表
     */
    @Query("SELECT u FROM BizItemUnit u WHERE u.isDeleted = false AND (u.isSystemDefault = true OR u.ownerId = :ownerId)")
    List<BizItemUnit> findAllByOwnerIdOrSystemDefault(@Param("ownerId") Long ownerId);

    /**
     * 根据单位ID、所有者ID查询未删除的单位。
     *
     * @param id      单位ID
     * @param ownerId 所有者ID
     * @return 符合条件的单位
     */
    Optional<BizItemUnit> findByIdAndOwnerIdAndIsDeletedFalse(Long id, Long ownerId);

    /**
     * 检查指定用户下是否已存在相同名称且未删除的单位。
     *
     * @param unitName 单位名称
     * @param ownerId  所有者ID
     * @return 若存在则返回true
     */
    boolean existsByUnitNameAndOwnerIdAndIsDeletedFalse(String unitName, Long ownerId);

    /**
     * 查询所有未删除的系统默认物品单位。
     *
     * @return 系统默认物品单位列表
     */
    List<BizItemUnit> findByIsSystemDefaultTrueAndIsDeletedFalse();
}
