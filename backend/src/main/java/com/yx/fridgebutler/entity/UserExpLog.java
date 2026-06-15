package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * 用户经验值变动日志实体类。
 * <p>对应数据库表 user_exp_log，用于审计和展示用户的经验值获取记录。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_exp_log", indexes = {
        @Index(name = "idx_user_date",
                columnList = "user_id, created_at")
})
public class UserExpLog {

    /**
     * 记录ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户ID，关联系统用户，必填。
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 行为类型，必填，最大长度50。
     * <p>如：LOGIN, NO_EXPIRE, CONSUME_EXPIRING, ADD_ITEM, ORGANIZE, VIEW_DATA_CENTER, AI_CHAT, BADGE, STREAK_BONUS, SCORE_BREAKTHROUGH, MONTHLY_REPORT, BIND_EMAIL, GUIDE, SHARE</p>
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    /**
     * 行为描述，最大长度200。
     */
    @Size(max = 200)
    @Column(name = "action_desc", length = 200)
    private String actionDesc;

    /**
     * 本次获得经验值，必填。
     */
    @NotNull
    @Column(name = "exp_gained", nullable = false)
    private Integer expGained;

    /**
     * 获得后的总经验值（total_exp），必填。
     */
    @NotNull
    @Column(name = "exp_balance", nullable = false)
    private Integer expBalance;

    /**
     * 关联业务ID（如物品ID、徽章ID）。
     */
    @Column(name = "related_id")
    private Long relatedId;

    /**
     * 创建时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP(3)")
    @Column(name = "created_at")
    private Instant createdAt;
}
