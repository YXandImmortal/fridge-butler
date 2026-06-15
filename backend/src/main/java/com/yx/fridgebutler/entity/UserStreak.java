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
 * 用户冰鲜连续天数实体类。
 * <p>对应数据库表 user_streak，用于存储用户的冰鲜连续天数、保护次数、自动保护设置等信息。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_streak")
public class UserStreak {

    /**
     * 记录ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户ID，关联系统用户，必填，唯一。
     */
    @NotNull
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * 当前连续冰鲜天数，默认值为 0。
     */
    @ColumnDefault("0")
    @Column(name = "current_streak")
    private Integer currentStreak;

    /**
     * 历史最高连续天数，默认值为 0。
     */
    @ColumnDefault("0")
    @Column(name = "max_streak")
    private Integer maxStreak;

    /**
     * 本月剩余保护次数，默认值为 2。
     */
    @ColumnDefault("2")
    @Column(name = "protect_count_remaining")
    private Integer protectCountRemaining;

    /**
     * 本月总保护次数（含额外获得的），默认值为 2。
     */
    @ColumnDefault("2")
    @Column(name = "protect_count_total")
    private Integer protectCountTotal;

    /**
     * 本月已使用保护次数，默认值为 0。
     */
    @ColumnDefault("0")
    @Column(name = "protect_count_used")
    private Integer protectCountUsed;

    /**
     * 保护次数重置月份，如 "2026-06"。
     */
    @ColumnDefault("''")
    @Column(name = "protect_reset_month", length = 7)
    private String protectResetMonth;

    /**
     * 是否自动使用保护：1-是 0-否，默认值为 1。
     */
    @ColumnDefault("1")
    @Column(name = "auto_protect_enabled")
    private Byte autoProtectEnabled;

    /**
     * 保护使用是否通知：1-是 0-否，默认值为 1。
     */
    @ColumnDefault("1")
    @Column(name = "protect_notify_enabled")
    private Byte protectNotifyEnabled;

    /**
     * 上次判定日期。
     */
    @Column(name = "last_check_date")
    private LocalDate lastCheckDate;

    /**
     * 上次判定结果：0-有过期 1-无过期，默认值为 0。
     */
    @ColumnDefault("0")
    @Column(name = "last_check_result")
    private Byte lastCheckResult;

    /**
     * 创建时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP(3)")
    @Column(name = "created_at")
    private Instant createdAt;

    /**
     * 更新时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP(3)")
    @Column(name = "updated_at")
    private Instant updatedAt;
}
