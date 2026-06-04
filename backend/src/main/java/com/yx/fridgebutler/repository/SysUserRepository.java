package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 系统用户数据访问层。
 * <p>提供系统用户实体的增删改查操作，以及按用户名、手机号查询和存在性校验的自定义方法。</p>
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 符合条件的用户Optional对象
     */
    Optional<SysUser> findByUsername(String username);

    /**
     * 根据用户名或手机号查询用户。
     *
     * @param username 用户名
     * @param mobile   手机号
     * @return 用户名或手机号匹配的用户Optional对象
     */
    Optional<SysUser> findByUsernameOrMobile(String username, String mobile);

    /**
     * 检查指定用户名是否已存在。
     *
     * @param username 用户名
     * @return true 表示已存在，false 表示不存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查指定手机号是否已存在。
     *
     * @param mobile 手机号
     * @return true 表示已存在，false 表示不存在
     */
    boolean existsByMobile(String mobile);

    /**
     * 统计未删除的用户总数。
     *
     * @return 未删除用户数量
     */
    long countByIsDeletedFalse();

    /**
     * 统计指定时间之后创建的未删除用户数量。
     *
     * @param createTime 创建时间阈值
     * @return 符合条件的用户数量
     */
    long countByIsDeletedFalseAndCreateTimeGreaterThanEqual(Instant createTime);

    /**
     * 统计指定时间之后有登录记录的未删除用户数量。
     *
     * @param lastLoginTime 最后登录时间阈值
     * @return 符合条件的用户数量
     */
    long countByIsDeletedFalseAndLastLoginTimeGreaterThanEqual(Instant lastLoginTime);

    /**
     * 查询指定时间之后的每日新增用户数量（原生SQL）。
     *
     * @param startDate 起始时间
     * @return [日期字符串, 数量] 列表
     */
    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM sys_user WHERE is_deleted = 0 AND create_time >= :startDate GROUP BY DATE(create_time) ORDER BY date",
            nativeQuery = true)
    List<Object[]> countNewUsersTrend(@Param("startDate") Instant startDate);

    /**
     * 查询指定时间之后的每日活跃用户数量（原生SQL）。
     *
     * @param startDate 起始时间
     * @return [日期字符串, 数量] 列表
     */
    @Query(value = "SELECT DATE(last_login_time) as date, COUNT(*) as count FROM sys_user WHERE is_deleted = 0 AND last_login_time >= :startDate GROUP BY DATE(last_login_time) ORDER BY date",
            nativeQuery = true)
    List<Object[]> countActiveUsersTrend(@Param("startDate") Instant startDate);

    /**
     * 分页查询用户列表（支持关键词和状态筛选）。
     * <p>关键词模糊匹配用户名或手机号；status 为 null 时查询全部。</p>
     *
     * @param keyword  关键词
     * @param status   状态（true=禁用，false=正常，null=全部）
     * @param pageable 分页参数
     * @return 用户分页数据
     */
    @Query("""
            SELECT u FROM SysUser u
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR u.username LIKE %:keyword%
                   OR u.mobile LIKE %:keyword%)
              AND (:status IS NULL OR u.isDeleted = :status)
            """)
    Page<SysUser> findByKeywordAndStatus(@Param("keyword") String keyword,
                                         @Param("status") Boolean status,
                                         Pageable pageable);
}
