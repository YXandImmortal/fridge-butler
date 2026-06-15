package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 用户行为计数器实体类。
 * <p>对应数据库表 user_action_counter，用于徽章解锁判定的高频行为计数。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_action_counter", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_type_date", columnNames = {"user_id", "counter_type", "count_date"})
})
public class UserActionCounter {

    /**
     * 记录ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户ID，关联系统用户。
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 计数类型。
     */
    @NotNull
    @Column(name = "counter_type", nullable = false, length = 50)
    private String counterType;

    /**
     * 累计计数，默认值为 0。
     */
    @ColumnDefault("0")
    @Column(name = "count_value")
    private Integer countValue;

    /**
     * 按日计数时使用（如 ORGANIZE_DAY）。
     */
    @Column(name = "count_date")
    private LocalDate countDate;

    /**
     * 更新时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP(3)")
    @Column(name = "updated_at")
    private Instant updatedAt;
}
