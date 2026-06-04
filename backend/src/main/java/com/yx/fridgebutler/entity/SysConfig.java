package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * 系统配置实体类。
 * <p>对应数据库表 sys_config，以 key-value 形式存储系统级配置项。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sys_config")
public class SysConfig {

    /**
     * 配置ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 配置键，唯一，用于程序中标识配置项。
     * <p>如：sys.announcement、sys.register_open 等。</p>
     */
    @Size(max = 64)
    @NotNull
    @Column(name = "config_key", nullable = false, unique = true, length = 64)
    private String configKey;

    /**
     * 配置值，支持长文本存储。
     */
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    /**
     * 配置项描述说明。
     */
    @Size(max = 255)
    @Column(name = "description")
    private String description;

    /**
     * 更新时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;
}
