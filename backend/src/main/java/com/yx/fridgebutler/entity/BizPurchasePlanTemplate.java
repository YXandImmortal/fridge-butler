package com.yx.fridgebutler.entity;

@lombok.Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "biz_purchase_plan_template", indexes = {@jakarta.persistence.Index(name = "idx_user_id",
columnList = "user_id")})
public class BizPurchasePlanTemplate {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "user_id", nullable = false)
private java.lang.Long userId;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "template_name", nullable = false, length = 100)
private java.lang.String templateName;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.persistence.Column(name = "scene_desc")
private java.lang.String sceneDesc;

@jakarta.validation.constraints.NotNull
@org.hibernate.annotations.ColumnDefault("0")
@jakarta.persistence.Column(name = "item_count", nullable = false)
private java.lang.Integer itemCount;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "create_time")
private java.time.Instant createTime;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "update_time")
private java.time.Instant updateTime;



}