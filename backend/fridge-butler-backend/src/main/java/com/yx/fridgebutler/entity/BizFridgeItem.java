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

/**
 * 冰箱物品实体类。
 * <p>对应数据库表 biz_fridge_item，用于存储冰箱中存放的物品信息，包括名称、数量、保质期、分类等。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_fridge_item")
public class BizFridgeItem {

    /**
     * 物品ID，主键，自增。
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
     * 物品名称，必填，最大长度50。
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "item_name", nullable = false, length = 50)
    private String itemName;

    /**
     * 物品单位ID，关联物品单位实体。
     */
    @Column(name = "item_unit_id")
    private Long itemUnitId;

    /**
     * 入库日期，可选。
     */
    @Column(name = "stored_date")
    private LocalDate storedDate;

    /**
     * 生产日期，可选。
     */
    @Column(name = "production_date")
    private LocalDate productionDate;

    /**
     * 保质期天数，可选。
     */
    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    /**
     * 操作人ID，关联系统用户，必填。
     */
    @NotNull
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    /**
     * 分类ID，关联物品分类实体。
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 物品数量，必填，精度为10位小数点后2位，默认值为 0.00。
     */
    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "item_num", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemNum;

    /**
     * 备注信息，可选，默认值为空字符串，最大长度255。
     */
    @Size(max = 255)
    @ColumnDefault("''")
    @Column(name = "remark")
    private String remark;

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
