package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 物品取出记录实体类。
 * <p>对应数据库表 biz_item_take_out_record，用于记录从冰箱中取出物品的操作信息，包括取出数量、剩余数量、操作人等。</p>
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_item_take_out_record", indexes = {
        @Index(name = "idx_item_id",
                columnList = "item_id"),
        @Index(name = "idx_fridge_id",
                columnList = "fridge_id"),
        @Index(name = "idx_operator_id",
                columnList = "operator_id"),
        @Index(name = "idx_create_time",
                columnList = "create_time")})
public class BizItemTakeOutRecord {

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
     * 物品名称，必填，最大长度50。
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "item_name", nullable = false, length = 50)
    private String itemName;

    /**
     * 取出数量，必填，精度为10位小数点后2位。
     */
    @NotNull
    @Column(name = "take_out_num", nullable = false, precision = 10, scale = 2)
    private BigDecimal takeOutNum;

    /**
     * 取出后剩余数量，必填，精度为10位小数点后2位。
     */
    @NotNull
    @Column(name = "remaining_num", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingNum;

    /**
     * 操作人ID，关联系统用户，必填。
     */
    @NotNull
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    /**
     * 操作时间，默认为当前时间戳（微秒精度）。
     */
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "create_time")
    private Instant createTime;

}
