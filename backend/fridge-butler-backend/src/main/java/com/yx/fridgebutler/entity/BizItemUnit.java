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
 * 物品单位实体类。
 * <p>对应数据库表 biz_item_unit，用于存储物品的计量单位信息（如斤、瓶、个等），支持系统默认单位和用户自定义单位。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_item_unit")
public class BizItemUnit {

    /**
     * 单位ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 单位名称，必填，最大长度20。
     */
    @Size(max = 20)
    @NotNull
    @Column(name = "unit_name", nullable = false, length = 20)
    private String unitName;

    /**
     * 单位类型ID，关联单位类型实体，必填。
     */
    @NotNull
    @Column(name = "unit_type_id", nullable = false)
    private Long unitTypeId;

    /**
     * 是否系统默认单位，true 表示系统默认，默认值为 true，必填。
     */
    @NotNull
    @ColumnDefault("1")
    @Column(name = "is_system_default", nullable = false)
    private Boolean isSystemDefault;

    /**
     * 所有者ID，关联系统用户，系统默认单位时为空。
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
