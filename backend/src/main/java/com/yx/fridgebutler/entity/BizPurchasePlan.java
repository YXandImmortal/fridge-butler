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
@Table(name = "biz_purchase_plan", indexes = {
        @Index(name = "idx_user_id",
                columnList = "user_id"),
        @Index(name = "idx_fridge_id",
                columnList = "fridge_id"),
        @Index(name = "idx_status",
                columnList = "plan_status")})
public class BizPurchasePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Size(max = 100)
    @NotNull
    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'MANUAL_CREATE'")
    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "plan_status", nullable = false)
    private Byte planStatus;

    @Size(max = 255)
    @Column(name = "scene_desc")
    private String sceneDesc;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "total_items", nullable = false)
    private Integer totalItems;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "completed_items", nullable = false)
    private Integer completedItems;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;


}