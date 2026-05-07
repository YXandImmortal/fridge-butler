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
 * 冰箱实体类。
 * <p>对应数据库表 biz_fridge，用于存储冰箱的基本信息，包括名称、地址、容量、所属用户等。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_fridge")
public class BizFridge {

    /**
     * 冰箱ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 冰箱名称，必填，最大长度30。
     */
    @Size(max = 30)
    @NotNull
    @Column(name = "fridge_name", nullable = false, length = 30)
    private String fridgeName;

    /**
     * 所有者ID，关联系统用户，必填。
     */
    @NotNull
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /**
     * 是否为默认冰箱，true 表示默认，默认值为 false。
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    /**
     * 冰箱地址，可选，最大长度255。
     */
    @Size(max = 255)
    @Column(name = "fridge_address")
    private String fridgeAddress;

    /**
     * 总容量，可选。
     */
    @Column(name = "total_capacity")
    private Integer totalCapacity;

    /**
     * 创建时间，默认为当前时间戳，必填。
     */
    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    /**
     * 状态，true 表示启用，默认值为 true。
     */
    @ColumnDefault("1")
    @Column(name = "status")
    private Boolean status;

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

    /**
     * 备注信息，可选，最大长度255。
     */
    @Size(max = 255)
    @Column(name = "remark")
    private String remark;

}
