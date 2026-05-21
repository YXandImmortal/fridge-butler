package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "rate", nullable = false)
    private Integer rate;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @Column(name = "total_capacity")
    private Integer totalCapacity;

    @Column(name = "fridge_type_id")
    private Long fridgeTypeId;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "last_calculate_time", nullable = false)
    private Instant lastCalculateTime;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Byte isDeleted;


}