package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.SysActivationKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 激活密钥数据访问层。
 * <p>提供激活密钥实体的增删改查，支持按密钥码查询、状态筛选、分页查询等。</p>
 */
@Repository
public interface SysActivationKeyRepository extends JpaRepository<SysActivationKey, Long> {

    /**
     * 根据密钥码查询密钥。
     *
     * @param keyCode 密钥字符串
     * @return 密钥Optional对象
     */
    Optional<SysActivationKey> findByKeyCode(String keyCode);

    /**
     * 检查指定密钥码是否已存在。
     *
     * @param keyCode 密钥字符串
     * @return true 表示已存在
     */
    boolean existsByKeyCode(String keyCode);

    /**
     * 分页查询密钥列表（支持关键词和状态筛选）。
     * <p>关键词模糊匹配密钥码或备注；status 为 null 时查询全部。</p>
     *
     * @param keyword  关键词
     * @param status   状态字符串
     * @param pageable 分页参数
     * @return 密钥分页数据
     */
    @Query("""
            SELECT k FROM SysActivationKey k
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR k.keyCode LIKE %:keyword%
                   OR k.remark LIKE %:keyword%)
              AND (:status IS NULL OR k.status = :status)
            """)
    Page<SysActivationKey> findByKeywordAndStatus(@Param("keyword") String keyword,
                                                   @Param("status") String status,
                                                   Pageable pageable);
}
