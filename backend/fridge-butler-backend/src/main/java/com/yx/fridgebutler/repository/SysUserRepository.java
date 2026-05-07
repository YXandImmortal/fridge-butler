package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
