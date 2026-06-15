package com.yx.fridgebutler.entity;

@lombok.Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "biz_purchase_plan", indexes = {
@jakarta.persistence.Index(name = "idx_user_id",
columnList = "user_id"),
@jakarta.persistence.Index(name = "idx_fridge_id",
columnList = "fridge_id"),
@jakarta.persistence.Index(name = "idx_status",
columnList = "plan_status")})
public class BizPurchasePlan {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "user_id", nullable = false)
private java.lang.Long userId;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "fridge_id", nullable = false)
private java.lang.Long fridgeId;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "plan_name", nullable = false, length = 100)
private java.lang.String planName;

@jakarta.validation.constraints.Size(max = 50)
@jakarta.validation.constraints.NotNull
@org.hibernate.annotations.ColumnDefault("'MANUAL_CREATE'")
@jakarta.persistence.Column(name = "source", nullable = false, length = 50)
private java.lang.String source;

@jakarta.validation.constraints.NotNull
@org.hibernate.annotations.ColumnDefault("1")
@jakarta.persistence.Column(name = "plan_status", nullable = false)
private java.lang.Byte planStatus;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.persistence.Column(name = "scene_desc")
private java.lang.String sceneDesc;

@jakarta.validation.constraints.NotNull
@org.hibernate.annotations.ColumnDefault("0")
@jakarta.persistence.Column(name = "total_items", nullable = false)
private java.lang.Integer totalItems;

@jakarta.validation.constraints.NotNull
@org.hibernate.annotations.ColumnDefault("0")
@jakarta.persistence.Column(name = "completed_items", nullable = false)
private java.lang.Integer completedItems;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "create_time")
private java.time.Instant createTime;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "update_time")
private java.time.Instant updateTime;



}