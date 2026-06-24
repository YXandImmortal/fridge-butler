package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_purchase_plan_template_item", indexes = {@Index(name = "idx_template_id",
        columnList = "template_id")})
public class BizPurchasePlanTemplateItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "template_id", nullable = false)
    private Long templateId;

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

    @ColumnDefault("0")
    @Column(name = "sort_order")
    private Integer sortOrder;

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