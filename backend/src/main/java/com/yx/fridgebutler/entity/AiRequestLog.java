package com.yx.fridgebutler.entity;

import com.yx.fridgebutler.enums.AiProvider;
import com.yx.fridgebutler.enums.AiRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ai_request_log", indexes = {
        @Index(name = "idx_ai_request_create_time", columnList = "create_time"),
        @Index(name = "idx_ai_request_status_time", columnList = "status,create_time")
})
public class AiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 32)
    private AiProvider provider;

    @Column(name = "model", nullable = false, length = 128)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AiRequestStatus status;

    @Column(name = "create_time", nullable = false)
    private Instant createTime;
}
