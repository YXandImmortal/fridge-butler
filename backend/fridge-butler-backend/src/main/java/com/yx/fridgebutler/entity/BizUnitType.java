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
 * 单位类型实体类。
 * <p>对应数据库表 biz_unit_type，用于存储物品单位类型信息（如重量类、数量类等），支持系统默认类型和用户自定义类型。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_unit_type")
public class BizUnitType {

    /**
     * 类型ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 单位类型名称，必填，最大长度20。
     */
    @Size(max = 20)
    @NotNull
    @Column(name = "unit_type_name", nullable = false, length = 20)
    private String unitTypeName;

    /**
     * 是否系统默认类型，true 表示系统默认，默认值为 true，必填。
     */
    @NotNull
    @ColumnDefault("1")
    @Column(name = "is_system_default", nullable = false)
    private Boolean isSystemDefault;

    /**
     * 所有者ID，关联系统用户，系统默认类型时为空。
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * 创建时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

    /**
     * 更新时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;

    /**
     * 是否删除，true 表示已删除，默认值为 false。
     */
    @ColumnDefault("0")
    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
