package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * 用户成就系统个性化设置实体类。
 * <p>对应数据库表 user_achievement_setting，用于存储用户是否隐藏成就面板、自动保护等偏好设置。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_achievement_setting")
public class UserAchievementSetting {

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
     * 是否隐藏成就面板：1-隐藏 0-展示，默认值为 0。
     */
    @ColumnDefault("0")
    @Column(name = "panel_hidden")
    private Byte panelHidden;

    /**
     * 是否自动使用冰鲜保护：1-是 0-否，默认值为 1。
     */
    @ColumnDefault("1")
    @Column(name = "auto_streak_protect")
    private Byte autoStreakProtect;

    /**
     * 保护使用是否通知：1-是 0-否，默认值为 1。
     */
    @ColumnDefault("1")
    @Column(name = "streak_protect_notify")
    private Byte streakProtectNotify;

    /**
     * 更新时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP(3)")
    @Column(name = "updated_at")
    private Instant updatedAt;
}
