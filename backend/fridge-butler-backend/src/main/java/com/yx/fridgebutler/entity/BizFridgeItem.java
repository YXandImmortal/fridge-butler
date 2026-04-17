package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_fridge_item")
public class BizFridgeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Size(max = 50)
    @NotNull
    @Column(name = "item_name", nullable = false, length = 50)
    private String itemName;

    @Column(name = "item_unit_id")
    private Long itemUnitId;

    @Column(name = "stored_date")
    private LocalDate storedDate;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @NotNull
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "category_id")
    private Long categoryId;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "item_num", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemNum;

    @Size(max = 255)
    @ColumnDefault("''")
    @Column(name = "remark")
    private String remark;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;

    @ColumnDefault("0")
    @Column(name = "is_deleted")
    private Boolean isDeleted;


}