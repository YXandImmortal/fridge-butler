package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.UserExp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户经验值与等级数据访问层。
 * <p>提供用户经验值实体的增删改查操作。</p>
 */
@Repository
public interface UserExpRepository extends JpaRepository<UserExp, Long> {

    /**
     * 根据用户ID查询经验值记录。
     *
     * @param userId 用户ID
     * @return 经验值记录
     */
    Optional<UserExp> findByUserId(Long userId);
}
