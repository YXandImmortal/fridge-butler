package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizUnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    /**
     * 根据单位类型ID、所有者ID查询未删除的单位类型。
     *
     * @param id      单位类型ID
     * @param ownerId 所有者ID
     * @return 符合条件的单位类型
     */
    Optional<BizUnitType> findByIdAndOwnerIdAndIsDeletedFalse(Long id, Long ownerId);

    /**
     * 检查指定用户下是否已存在相同名称且未删除的单位类型。
     *
     * @param unitTypeName 单位类型名称
     * @param ownerId      所有者ID
     * @return 若存在则返回true
     */
    boolean existsByUnitTypeNameAndOwnerIdAndIsDeletedFalse(String unitTypeName, Long ownerId);

    /**
     * 查询所有未删除的系统默认单位类型。
     *
     * @return 系统默认单位类型列表
     */
    List<BizUnitType> findByIsSystemDefaultTrueAndIsDeletedFalse();
}
