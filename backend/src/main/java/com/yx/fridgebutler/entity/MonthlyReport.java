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

/**
 * 用户月度报告实体类。
 * <p>对应数据库表 monthly_report，用于存储用户每月的成就与冰箱管理聚合报告。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "monthly_report", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_month", columnNames = {"user_id", "`year_month`"})
})
public class MonthlyReport {

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
     * 报告年月，如 2026-05。
     */
    @NotNull
    @Column(name = "`year_month`", nullable = false, length = 7)
    private String yearMonth;

    /**
     * 当月平均保鲜评分。
     */
    @ColumnDefault("0")
    @Column(name = "avg_score")
    private Integer avgScore;

    /**
     * 当月最高保鲜评分。
     */
    @ColumnDefault("0")
    @Column(name = "max_score")
    private Integer maxScore;

    /**
     * 当月最低保鲜评分。
     */
    @ColumnDefault("0")
    @Column(name = "min_score")
    private Integer minScore;

    /**
     * 当月过期物品数。
     */
    @ColumnDefault("0")
    @Column(name = "expired_count")
    private Integer expiredCount;

    /**
     * 当月临期物品数。
     */
    @ColumnDefault("0")
    @Column(name = "expiring_count")
    private Integer expiringCount;

    /**
     * AI估算浪费金额（元）。
     */
    @ColumnDefault("0")
    @Column(name = "wasted_amount", precision = 10, scale = 2)
    private BigDecimal wastedAmount;

    /**
     * 避免浪费食材重量（kg）。
     */
    @ColumnDefault("0")
    @Column(name = "saved_kg", precision = 10, scale = 2)
    private BigDecimal savedKg;

    /**
     * 减少CO2排放（kg）。
     */
    @ColumnDefault("0")
    @Column(name = "co2_saved", precision = 10, scale = 2)
    private BigDecimal co2Saved;

    /**
     * 节约用水（L）。
     */
    @ColumnDefault("0")
    @Column(name = "water_saved", precision = 10, scale = 2)
    private BigDecimal waterSaved;

    /**
     * 当月添加物品数。
     */
    @ColumnDefault("0")
    @Column(name = "items_added")
    private Integer itemsAdded;

    /**
     * 当月取出物品数。
     */
    @ColumnDefault("0")
    @Column(name = "items_taken_out")
    private Integer itemsTakenOut;

    /**
     * 当月新解锁徽章数。
     */
    @ColumnDefault("0")
    @Column(name = "new_badges")
    private Integer newBadges;

    /**
     * 月初等级。
     */
    @ColumnDefault("1")
    @Column(name = "level_start")
    private Integer levelStart;

    /**
     * 月末等级。
     */
    @ColumnDefault("1")
    @Column(name = "level_end")
    private Integer levelEnd;

    /**
     * 当月最高连续天数。
     */
    @ColumnDefault("0")
    @Column(name = "streak_max")
    private Integer streakMax;

    /**
     * 首次查看时间（用于EXP发放控制）。
     */
    @Column(name = "viewed_at")
    private Instant viewedAt;

    /**
     * 生成时间。
     */
    @ColumnDefault("CURRENT_TIMESTAMP(3)")
    @Column(name = "generated_at")
    private Instant generatedAt;
}
