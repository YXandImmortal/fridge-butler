package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 32)
    @NotNull
    @Column(name = "session_id", nullable = false, length = 32)
    private String sessionId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Size(max = 100)
    @Column(name = "title", length = 100)
    private String title;

    @NotNull
    @Column(name = "last_active_time", nullable = false)
    private Instant lastActiveTime;

    @ColumnDefault("0")
    @Column(name = "is_deleted")
    private Byte isDeleted;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;


}