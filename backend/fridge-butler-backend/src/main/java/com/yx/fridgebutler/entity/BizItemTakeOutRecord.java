package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @NotNull
    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Size(max = 50)
    @NotNull
    @Column(name = "item_name", nullable = false, length = 50)
    private String itemName;

    @NotNull
    @Column(name = "take_out_num", nullable = false, precision = 10, scale = 2)
    private BigDecimal takeOutNum;

    @NotNull
    @Column(name = "remaining_num", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingNum;

    @NotNull
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "create_time")
    private Instant createTime;


}