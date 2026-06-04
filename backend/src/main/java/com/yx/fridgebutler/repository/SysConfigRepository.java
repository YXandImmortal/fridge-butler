package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 系统配置数据访问层。
 * <p>提供系统配置实体的增删改查，支持按配置键查询。</p>
 */
@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {

    /**
     * 根据配置键查询配置项。
     *
     * @param configKey 配置键
     * @return 配置项Optional对象
     */
    Optional<SysConfig> findByConfigKey(String configKey);
}
