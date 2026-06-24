package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_purchase_plan_template", indexes = {@Index(name = "idx_user_id",
        columnList = "user_id")})
public class BizPurchasePlanTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Size(max = 100)
    @NotNull
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Size(max = 255)
    @Column(name = "scene_desc")
    private String sceneDesc;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;


}