package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 物品分类数据访问层。
 * <p>提供物品分类实体的增删改查操作，以及按所有者查询系统默认分类和自定义分类的方法。</p>
 */
@Repository
public interface BizItemCategoryRepository extends JpaRepository<BizItemCategory, Long> {

    /**
     * 查询指定用户可用的分类列表，包括系统默认分类和该用户的自定义分类。
     *
     * @param ownerId 所有者ID
     * @return 未删除的系统默认分类或属于该用户的分类列表
     */
    @Query("SELECT c FROM BizItemCategory c WHERE c.isDeleted = false AND (c.isSystemDefault = true OR c.ownerId = :ownerId)")
    List<BizItemCategory> findAllByOwnerIdOrSystemDefault(@Param("ownerId") Long ownerId);

    /**
     * 根据分类ID、所有者ID查询未删除的分类。
     *
     * @param id      分类ID
     * @param ownerId 所有者ID
     * @return 符合条件的分类
     */
    Optional<BizItemCategory> findByIdAndOwnerIdAndIsDeletedFalse(Long id, Long ownerId);

    /**
     * 检查指定用户下是否已存在相同名称且未删除的分类。
     *
     * @param categoryName 分类名称
     * @param ownerId      所有者ID
     * @return 若存在则返回true
     */
    boolean existsByCategoryNameAndOwnerIdAndIsDeletedFalse(String categoryName, Long ownerId);

    /**
     * 查询所有未删除的系统默认物品分类。
     *
     * @return 系统默认物品分类列表
     */
    List<BizItemCategory> findByIsSystemDefaultTrueAndIsDeletedFalse();
}
