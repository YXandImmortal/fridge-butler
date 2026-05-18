package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * 物品变更记录实体类。
 * <p>对应数据库表 biz_item_change_record，用于记录物品属性变更的操作信息，包括变更字段、旧值、新值、操作人等。</p>
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_item_change_record", indexes = {
        @Index(name = "idx_item_id",
                columnList = "item_id"),
        @Index(name = "idx_fridge_id",
                columnList = "fridge_id"),
        @Index(name = "idx_operator_id",
                columnList = "operator_id"),
        @Index(name = "idx_create_time",
                columnList = "create_time"),
        @Index(name = "idx_change_type",
                columnList = "change_type")})
public class BizItemChangeRecord {

    /**
     * 记录ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 物品ID，关联冰箱物品实体，必填。
     */
    @NotNull
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    /**
     * 冰箱ID，关联冰箱实体，必填。
     */
    @NotNull
    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    /**
     * 变更类型，必填，最大长度50。
     * <p>如：UPDATE_NAME、UPDATE_NUM、UPDATE_SHELF_LIFE、UPDATE_CATEGORY、UPDATE_UNIT、UPDATE_STORED_DATE、UPDATE_PRODUCTION_DATE、UPDATE_REMARK 等</p>
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    /**
     * 变更字段名，必填，最大长度50。
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;

    /**
     * 变更前值，最大长度255。
     */
    @Size(max = 255)
    @Column(name = "old_value", length = 255)
    private String oldValue;

    /**
     * 变更后值，最大长度255。
     */
    @Size(max = 255)
    @Column(name = "new_value", length = 255)
    private String newValue;

    /**
     * 操作人ID，关联系统用户，必填。
     */
    @NotNull
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    /**
     * 备注信息，最大长度255。
     */
    @Size(max = 255)
    @Column(name = "remark", length = 255)
    private String remark;

    /**
     * 操作时间，默认为当前时间戳（微秒精度）。
     */
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "create_time")
    private Instant createTime;

}
