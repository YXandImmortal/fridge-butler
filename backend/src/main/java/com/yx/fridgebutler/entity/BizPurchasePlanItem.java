package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_purchase_plan_item", indexes = {
        @Index(name = "idx_plan_id",
                columnList = "plan_id"),
        @Index(name = "idx_status",
                columnList = "status")})
public class BizPurchasePlanItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Size(max = 100)
    @NotNull
    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "category_id")
    private Long categoryId;

    @NotNull
    @Column(name = "planned_num", nullable = false, precision = 10, scale = 2)
    private BigDecimal plannedNum;

    @NotNull
    @Column(name = "item_unit_id", nullable = false)
    private Long itemUnitId;

    @Column(name = "actual_num", precision = 10, scale = 2)
    private BigDecimal actualNum;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Size(max = 100)
    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "status", nullable = false)
    private Byte status;

    @Size(max = 255)
    @Column(name = "remark")
    private String remark;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;
    @NotNull
    @ColumnDefault("1")
    @Column(name = "store_in_fridge", nullable = false)
    private Boolean storeInFridge;


}