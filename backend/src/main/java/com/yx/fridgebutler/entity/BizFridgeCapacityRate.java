package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * 冰箱容量使用率实体类。
 * <p>对应数据库表 biz_fridge_capacity_rate，用于存储冰箱容量使用情况的统计信息。</p>
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_fridge_capacity_rate", indexes = {@Index(name = "idx_calculate_time",
        columnList = "last_calculate_time")}, uniqueConstraints = {@UniqueConstraint(name = "uk_fridge_id",
        columnNames = {"fridge_id"})})
public class BizFridgeCapacityRate {

    /**
     * 记录ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 冰箱ID，关联冰箱实体，必填。
     */
    @NotNull
    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    /**
     * 使用率（0-100 的整数百分比），必填，默认值为 0。
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "rate", nullable = false)
    private Integer rate;

    /**
     * 物品数量，必填，默认值为 0。
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    /**
     * 总容量。
     */
    @Column(name = "total_capacity")
    private Integer totalCapacity;

    /**
     * 冰箱类型ID，关联冰箱类型实体。
     */
    @Column(name = "fridge_type_id")
    private Long fridgeTypeId;

    /**
     * 最后计算时间，默认为当前时间戳，必填。
     */
    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "last_calculate_time", nullable = false)
    private Instant lastCalculateTime;

    /**
     * 创建时间，默认为当前时间戳，必填。
     */
    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    /**
     * 更新时间，默认为当前时间戳，必填。
     */
    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    /**
     * 是否删除，true 表示已删除，默认值为 0（未删除），必填。
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Byte isDeleted;

}
