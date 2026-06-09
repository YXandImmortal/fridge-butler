package com.yx.fridgebutler.entity;

import com.yx.fridgebutler.enums.DailyTipType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 每日小贴士实体。
 * <p>存储由 AI 生成的每日冰箱相关小贴士。</p>
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sys_daily_tip", indexes = {
        @Index(name = "idx_tip_date", columnList = "tip_date")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_tip_date", columnNames = {"tip_date"})
})
public class SysDailyTip {

    /**
     * 小贴士ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 小贴士类型，必填。
     * <p>如：FACT（冷知识）、TIP（实用技巧）、JOKE（冷笑话）、RIDDLE（谜语）等。</p>
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tip_type", nullable = false, length = 20)
    private DailyTipType tipType;

    /**
     * 表情符号，必填，最大长度10。
     */
    @Size(max = 10)
    @NotNull
    @Column(name = "emoji", nullable = false, length = 10)
    private String emoji;

    /**
     * 标题，必填，最大长度20。
     */
    @Size(max = 20)
    @NotNull
    @Column(name = "title", nullable = false, length = 20)
    private String title;

    /**
     * 内容，必填，长文本存储。
     */
    @NotNull
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    /**
     * 小贴士日期，必填，唯一。
     */
    @NotNull
    @Column(name = "tip_date", nullable = false)
    private LocalDate tipDate;

    /**
     * 答案或补充说明，长文本存储。
     * <p>在 RIDDLE 类型下为谜底，其他类型下存储每日小贴士正文内容。</p>
     */
    @Lob
    @Column(name = "answer")
    private String answer;

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
