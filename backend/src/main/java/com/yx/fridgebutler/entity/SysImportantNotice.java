package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * 重要通知模板实体。
 * <p>用于存储管理员发布的重要广播通知模板，新用户注册时从此表读取最新通知并生成个人通知记录。</p>
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sys_important_notice", indexes = {
        @Index(name = "idx_important_notice_create_time", columnList = "create_time")
})
public class SysImportantNotice {

    /**
     * 通知ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

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
     * 优先级，必填，默认值为 0。
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "priority", nullable = false)
    private Byte priority;

    /**
     * 创建时间，默认为当前时间戳，必填。
     */
    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    /**
     * 是否删除，true 表示已删除，默认值为 0（未删除），必填。
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Byte isDeleted;
}
