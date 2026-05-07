package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
