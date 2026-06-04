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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tip_type", nullable = false, length = 20)
    private DailyTipType tipType;

    @Size(max = 10)
    @NotNull
    @Column(name = "emoji", nullable = false, length = 10)
    private String emoji;

    @Size(max = 20)
    @NotNull
    @Column(name = "title", nullable = false, length = 20)
    private String title;

    @NotNull
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull
    @Column(name = "tip_date", nullable = false)
    private LocalDate tipDate;

    @Lob
    @Column(name = "answer")
    private String answer;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;
}
