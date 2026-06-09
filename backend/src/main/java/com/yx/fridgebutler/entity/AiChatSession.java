package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * AI聊天会话实体类。
 * <p>对应数据库表 ai_chat_session，用于存储用户与AI的聊天会话信息。</p>
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ai_chat_session", indexes = {@Index(name = "idx_user_active",
        columnList = "user_id, last_active_time")}, uniqueConstraints = {@UniqueConstraint(name = "uk_session",
        columnNames = {"session_id"})})
public class AiChatSession {

    /**
     * 会话记录ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 会话ID，必填，最大长度32，唯一标识一个会话。
     */
    @Size(max = 32)
    @NotNull
    @Column(name = "session_id", nullable = false, length = 32)
    private String sessionId;

    /**
     * 用户ID，关联系统用户，必填。
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 会话标题，最大长度100。
     */
    @Size(max = 100)
    @Column(name = "title", length = 100)
    private String title;

    /**
     * 最后活跃时间，必填。
     */
    @NotNull
    @Column(name = "last_active_time", nullable = false)
    private Instant lastActiveTime;

    /**
     * 是否删除，true 表示已删除，默认值为 0（未删除）。
     */
    @ColumnDefault("0")
    @Column(name = "is_deleted")
    private Byte isDeleted;

    /**
     * 创建时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

    /**
     * 更新时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;

}
