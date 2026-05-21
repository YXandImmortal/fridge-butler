package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizFridgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 冰箱类型数据访问层。
 * <p>提供冰箱类型实体的增删改查操作，支持按ID查询未删除的系统预设类型。</p>
 */
@Repository
public interface BizFridgeTypeRepository extends JpaRepository<BizFridgeType, Long> {

    /**
     * 根据类型ID查询未删除的冰箱类型。
     *
     * @param id 类型ID
     * @return 冰箱类型Optional对象
     */
    @Query("SELECT t FROM BizFridgeType t WHERE t.id = :id AND t.isDeleted = false")
    Optional<BizFridgeType> findByIdAndIsDeletedFalse(@Param("id") Long id);

    /**
     * 查询所有未删除的冰箱类型。
     *
     * @return 冰箱类型列表
     */
    @Query("SELECT t FROM BizFridgeType t WHERE t.isDeleted = false ORDER BY t.id")
    List<BizFridgeType> findAllByIsDeletedFalse();
}
