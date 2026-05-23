package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ai_chat_message", indexes = {@Index(name = "idx_session_time",
        columnList = "session_id, create_time")})
public class AiChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 32)
    @NotNull
    @Column(name = "session_id", nullable = false, length = 32)
    private String sessionId;

    @Size(max = 20)
    @NotNull
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Lob
    @Column(name = "content")
    private String content;

    @Size(max = 30)
    @Column(name = "message_type", length = 30)
    private String messageType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_data")
    private Map<String, Object> structuredData;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments")
    private List<Map<String, Object>> attachments;


}