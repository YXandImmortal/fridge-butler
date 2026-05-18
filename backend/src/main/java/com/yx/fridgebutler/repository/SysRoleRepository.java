package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 系统角色数据访问层。
 * <p>提供系统角色实体的增删改查操作，继承 JpaRepository 使用基础 CRUD 方法。</p>
 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {
}
