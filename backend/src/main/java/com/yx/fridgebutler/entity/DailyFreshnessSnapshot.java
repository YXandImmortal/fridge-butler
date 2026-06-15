package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 每日保鲜评分快照实体类。
 * <p>对应数据库表 daily_freshness_snapshot，用于存储用户每日的保鲜评分快照数据。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "daily_freshness_snapshot", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_date", columnNames = {"user_id", "snapshot_date"})
})
public class DailyFreshnessSnapshot {

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
     * 快照日期。
     */
    @NotNull
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    /**
     * 保鲜评分（0-100）。
     */
    @Column(name = "freshness_score")
    private Integer freshnessScore;

    /**
     * 评分等级（S/A/B/C/D）。
     */
    @Column(name = "score_grade", length = 1)
    private String scoreGrade;

    /**
     * 当日是否有过期物品：1-是 0-否。
     */
    @ColumnDefault("0")
    @Column(name = "has_expired")
    private Byte hasExpired;

    /**
     * 当日物品总数。
     */
    @ColumnDefault("0")
    @Column(name = "item_count")
    private Integer itemCount;

    /**
     * 当日过期物品数。
     */
    @ColumnDefault("0")
    @Column(name = "expired_count")
    private Integer expiredCount;

    /**
     * 当日3天内临期物品数。
     */
    @ColumnDefault("0")
    @Column(name = "expiring_3d_count")
    private Integer expiring3dCount;

    /**
     * 当日冰箱数量。
     */
    @ColumnDefault("0")
    @Column(name = "fridge_count")
    private Integer fridgeCount;

    /**
     * 当日平均容量利用率。
     */
    @Column(name = "capacity_rate_avg", precision = 5, scale = 2)
    private BigDecimal capacityRateAvg;

    /**
     * 新鲜度维度得分（0-100）。
     */
    @ColumnDefault("0")
    @Column(name = "freshness_score_freshness", precision = 5, scale = 2)
    private BigDecimal freshnessScoreFreshness;

    /**
     * 周转效率维度得分（0-100）。
     */
    @ColumnDefault("0")
    @Column(name = "freshness_score_turnover", precision = 5, scale = 2)
    private BigDecimal freshnessScoreTurnover;

    /**
     * 过期控制维度得分（0-100）。
     */
    @ColumnDefault("0")
    @Column(name = "freshness_score_expired", precision = 5, scale = 2)
    private BigDecimal freshnessScoreExpired;

    /**
     * 空间利用维度得分（0-100）。
     */
    @ColumnDefault("0")
    @Column(name = "freshness_score_capacity", precision = 5, scale = 2)
    private BigDecimal freshnessScoreCapacity;

    /**
     * 创建时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP(3)")
    @Column(name = "created_at")
    private Instant createdAt;
}
