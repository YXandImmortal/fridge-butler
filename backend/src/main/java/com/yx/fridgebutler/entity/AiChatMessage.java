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

/**
 * AI聊天消息实体类。
 * <p>对应数据库表 ai_chat_message，用于存储AI聊天会话中的消息记录。</p>
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ai_chat_message", indexes = {@Index(name = "idx_session_time",
        columnList = "session_id, create_time")})
public class AiChatMessage {

    /**
     * 消息ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 会话ID，必填，最大长度32。
     */
    @Size(max = 32)
    @NotNull
    @Column(name = "session_id", nullable = false, length = 32)
    private String sessionId;

    /**
     * 消息角色，必填，最大长度20。
     * <p>如：user（用户）、assistant（AI助手）、system（系统）等。</p>
     */
    @Size(max = 20)
    @NotNull
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    /**
     * 消息内容，长文本存储。
     */
    @Lob
    @Column(name = "content")
    private String content;

    /**
     * 消息类型，最大长度30。
     */
    @Size(max = 30)
    @Column(name = "message_type", length = 30)
    private String messageType;

    /**
     * 结构化数据，以JSON格式存储。
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_data")
    private Map<String, Object> structuredData;

    /**
     * 创建时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

    /**
     * 附件信息，以JSON格式存储。
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments")
    private List<Map<String, Object>> attachments;

}
