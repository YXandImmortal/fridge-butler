package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsername(String username);

    Optional<SysUser> findByUsernameOrMobile(String username, String mobile);

    boolean existsByUsername(String username);

    boolean existsByMobile(String mobile);
}
