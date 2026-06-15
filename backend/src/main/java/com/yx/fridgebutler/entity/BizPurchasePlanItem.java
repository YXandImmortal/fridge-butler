package com.yx.fridgebutler.entity;

@lombok.Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "biz_purchase_plan_item", indexes = {
@jakarta.persistence.Index(name = "idx_plan_id",
columnList = "plan_id"),
@jakarta.persistence.Index(name = "idx_status",
columnList = "status")})
public class BizPurchasePlanItem {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "plan_id", nullable = false)
private java.lang.Long planId;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "item_name", nullable = false, length = 100)
private java.lang.String itemName;

@jakarta.persistence.Column(name = "category_id")
private java.lang.Long categoryId;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "planned_num", nullable = false, precision = 10, scale = 2)
private java.math.BigDecimal plannedNum;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "item_unit_id", nullable = false)
private java.lang.Long itemUnitId;

@jakarta.persistence.Column(name = "actual_num", precision = 10, scale = 2)
private java.math.BigDecimal actualNum;

@jakarta.persistence.Column(name = "production_date")
private java.time.LocalDate productionDate;

@jakarta.persistence.Column(name = "shelf_life_days")
private java.lang.Integer shelfLifeDays;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.persistence.Column(name = "storage_location", length = 100)
private java.lang.String storageLocation;

@jakarta.validation.constraints.NotNull
@org.hibernate.annotations.ColumnDefault("1")
@jakarta.persistence.Column(name = "status", nullable = false)
private java.lang.Byte status;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.persistence.Column(name = "remark")
private java.lang.String remark;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "create_time")
private java.time.Instant createTime;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "update_time")
private java.time.Instant updateTime;



}