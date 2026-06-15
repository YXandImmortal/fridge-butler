package com.yx.fridgebutler.entity;

@lombok.Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "biz_purchase_plan_template_item", indexes = {@jakarta.persistence.Index(name = "idx_template_id",
columnList = "template_id")})
public class BizPurchasePlanTemplateItem {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "template_id", nullable = false)
private java.lang.Long templateId;

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

@org.hibernate.annotations.ColumnDefault("0")
@jakarta.persistence.Column(name = "sort_order")
private java.lang.Integer sortOrder;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "create_time")
private java.time.Instant createTime;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "update_time")
private java.time.Instant updateTime;



}