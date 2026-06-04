package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sys_notification", indexes = {
        @Index(name = "idx_notification_user_item_type",
                columnList = "user_id, item_id, type, status, is_deleted"),
        @Index(name = "idx_notification_user_type",
                columnList = "user_id, type, status, is_deleted"),
        @Index(name = "idx_notification_user_status",
                columnList = "user_id, status, is_deleted"),
        @Index(name = "idx_notification_user_time",
                columnList = "user_id, is_deleted, create_time"),
        @Index(name = "idx_notification_fridge_type_status",
                columnList = "fridge_id, type, status, is_deleted")})
public class SysNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fridge_id")
    private Long fridgeId;

    @Column(name = "item_id")
    private Long itemId;

    @Size(max = 100)
    @NotNull
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Size(max = 30)
    @NotNull
    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "priority", nullable = false)
    private Byte priority;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "status", nullable = false)
    private Byte status;

    @Size(max = 30)
    @Column(name = "action_type", length = 30)
    private String actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_payload")
    private Map<String, Object> actionPayload;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    @Column(name = "read_time")
    private Instant readTime;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Byte isDeleted;


}