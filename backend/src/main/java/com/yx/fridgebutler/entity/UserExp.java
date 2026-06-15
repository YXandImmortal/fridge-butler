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
 * 用户经验值与等级实体类。
 * <p>对应数据库表 user_exp，用于存储用户的经验值、等级、每日经验值上限等信息。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_exp")
public class UserExp {

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
     * 当前等级下的经验值（进度条用），默认值为 0。
     */
    @ColumnDefault("0")
    @Column(name = "current_exp")
    private Integer currentExp;

    /**
     * 累计获得的总经验值，默认值为 0。
     */
    @ColumnDefault("0")
    @Column(name = "total_exp")
    private Integer totalExp;

    /**
     * 当前等级，默认值为 1。
     */
    @ColumnDefault("1")
    @Column(name = "current_level")
    private Integer currentLevel;

    /**
     * 今日已获得经验值，默认值为 0。
     */
    @ColumnDefault("0")
    @Column(name = "daily_exp_today")
    private Integer dailyExpToday;

    /**
     * 今日经验值对应日期（用于跨天重置）。
     */
    @Column(name = "daily_exp_date")
    private LocalDate dailyExpDate;

    /**
     * Lv.8 后可自定义称号，null 则使用默认称号。
     */
    @Column(name = "title_custom", length = 50)
    private String titleCustom;

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
