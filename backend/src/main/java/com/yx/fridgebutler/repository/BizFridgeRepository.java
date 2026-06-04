package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizFridge;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 冰箱数据访问层。
 * <p>提供冰箱实体的增删改查操作，以及按所有者ID、关键字搜索、默认冰箱设置等自定义查询方法。</p>
 */
@Repository
public interface BizFridgeRepository extends JpaRepository<BizFridge, Long> {

    /**
     * 根据所有者ID查询未删除的冰箱列表，并按指定规则排序。
     *
     * @param ownerId 所有者ID
     * @param sort    排序规则
     * @return 该用户未删除的冰箱列表
     */
    List<BizFridge> findByOwnerIdAndIsDeletedFalse(Long ownerId, Sort sort);

    /**
     * 根据冰箱ID、所有者ID查询未删除的冰箱。
     *
     * @param id      冰箱ID
     * @param ownerId 所有者ID
     * @return 符合条件的冰箱Optional对象
     */
    Optional<BizFridge> findByIdAndOwnerIdAndIsDeletedFalse(Long id, Long ownerId);

    /**
     * 检查指定用户下是否存在指定名称且未删除的冰箱。
     *
     * @param fridgeName 冰箱名称
     * @param ownerId    所有者ID
     * @return true 表示存在，false 表示不存在
     */
    boolean existsByFridgeNameAndOwnerIdAndIsDeletedFalse(String fridgeName, Long ownerId);

    /**
     * 取消指定用户的默认冰箱设置。
     * <p>将该用户下所有未删除且标记为默认的冰箱的 isDefault 字段设为 false。</p>
     *
     * @param ownerId 所有者ID
     */
    @Modifying
    @Query("UPDATE BizFridge f SET f.isDefault = false WHERE f.ownerId = ?1 AND f.isDeleted = false AND f.isDefault = true")
    void unsetDefaultByOwnerId(Long ownerId);

    /**
     * 根据所有者ID和关键字搜索冰箱（按相关性和创建时间排序）。
     * <p>支持按冰箱名称、地址、备注进行模糊匹配，并按匹配字段的相关性降序排列。</p>
     *
     * @param ownerId 所有者ID
     * @param keyword 搜索关键字，可为空
     * @return 符合条件的冰箱列表
     */
    @Query("""
            SELECT f FROM BizFridge f
            WHERE f.ownerId = :ownerId
              AND f.isDeleted = false
              AND (:keyword IS NULL OR :keyword = ''
                   OR f.fridgeName LIKE :keyword
                   OR f.fridgeAddress LIKE :keyword
                   OR f.remark LIKE :keyword)
              AND (:fridgeTypeId IS NULL OR f.fridgeTypeId = :fridgeTypeId)
            ORDER BY
                CASE WHEN f.fridgeName LIKE :keyword THEN 3 ELSE 0 END DESC,
                CASE WHEN f.fridgeAddress LIKE :keyword THEN 2 ELSE 0 END DESC,
                CASE WHEN f.remark LIKE :keyword THEN 1 ELSE 0 END DESC,
                f.createTime DESC
            """)
    List<BizFridge> searchByKeyword(@Param("ownerId") Long ownerId, @Param("keyword") String keyword, @Param("fridgeTypeId") Long fridgeTypeId);

    /**
     * 根据所有者ID和关键字搜索冰箱（自定义排序）。
     * <p>支持按冰箱名称、地址、备注进行模糊匹配。</p>
     *
     * @param ownerId 所有者ID
     * @param keyword 搜索关键字，可为空
     * @param sort    排序规则
     * @return 符合条件的冰箱列表
     */
    @Query("""
            SELECT f FROM BizFridge f
            WHERE f.ownerId = :ownerId
              AND f.isDeleted = false
              AND (:keyword IS NULL OR :keyword = ''
                   OR f.fridgeName LIKE :keyword
                   OR f.fridgeAddress LIKE :keyword
                   OR f.remark LIKE :keyword)
              AND (:fridgeTypeId IS NULL OR f.fridgeTypeId = :fridgeTypeId)
            """)
    List<BizFridge> searchByKeyword(@Param("ownerId") Long ownerId, @Param("keyword") String keyword, @Param("fridgeTypeId") Long fridgeTypeId, Sort sort);

    /**
     * 查询指定用户未删除的默认冰箱。
     *
     * @param ownerId 所有者ID
     * @return 该用户的默认冰箱Optional对象
     */
    Optional<BizFridge> findByOwnerIdAndIsDefaultTrueAndIsDeletedFalse(Long ownerId);

    /**
     * 统计未删除的冰箱总数。
     *
     * @return 未删除冰箱数量
     */
    long countByIsDeletedFalse();

    /**
     * 按冰箱类型分组统计未删除的冰箱数量（原生SQL）。
     * <p>LEFT JOIN 冰箱类型表，未分类冰箱的类型名称返回 NULL。</p>
     *
     * @return [类型名称, 数量] 列表
     */
    @Query(value = """
            SELECT t.type_name as name, COUNT(f.id) as value
            FROM biz_fridge f
            LEFT JOIN biz_fridge_type t ON f.fridge_type_id = t.id
            WHERE f.is_deleted = 0
            GROUP BY f.fridge_type_id, t.type_name
            ORDER BY value DESC
            """, nativeQuery = true)
    List<Object[]> countFridgeGroupByType();
}
