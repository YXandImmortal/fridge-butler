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

/**
 * 系统通知实体类。
 * <p>对应数据库表 sys_notification，用于存储用户的系统通知消息，包括物品过期提醒、系统公告等。</p>
 */
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

    /**
     * 通知ID，主键，自增。
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
     * 冰箱ID，关联冰箱实体。
     */
    @Column(name = "fridge_id")
    private Long fridgeId;

    /**
     * 物品ID，关联冰箱物品实体。
     */
    @Column(name = "item_id")
    private Long itemId;

    /**
     * 通知标题，必填，最大长度100。
     */
    @Size(max = 100)
    @NotNull
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 通知内容，长文本存储。
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 通知类型，必填，最大长度30。
     */
    @Size(max = 30)
    @NotNull
    @Column(name = "type", nullable = false, length = 30)
    private String type;

    /**
     * 优先级，必填，默认值为 0。
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "priority", nullable = false)
    private Byte priority;

    /**
     * 状态，必填，默认值为 0。
     * <p>如：0-未读，1-已读等。</p>
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "status", nullable = false)
    private Byte status;

    /**
     * 动作类型，最大长度30。
     * <p>用于前端跳转或处理，如：OPEN_ITEM、OPEN_FRIDGE 等。</p>
     */
    @Size(max = 30)
    @Column(name = "action_type", length = 30)
    private String actionType;

    /**
     * 动作参数，以JSON格式存储。
     * <p>用于存储动作类型所需的额外参数。</p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_payload")
    private Map<String, Object> actionPayload;

    /**
     * 创建时间，默认为当前时间戳，必填。
     */
    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    /**
     * 读取时间。
     */
    @Column(name = "read_time")
    private Instant readTime;

    /**
     * 是否删除，true 表示已删除，默认值为 0（未删除），必填。
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Byte isDeleted;

}
