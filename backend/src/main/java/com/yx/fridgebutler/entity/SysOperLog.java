package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * 操作日志实体类。
 * <p>对应数据库表 sys_oper_log，用于持久化 HTTP 请求日志。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sys_oper_log")
public class SysOperLog {

    /**
     * 日志ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 链路追踪ID。
     */
    @Column(name = "trace_id", length = 64)
    private String traceId;

    /**
     * 操作用户ID。
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 操作用户名。
     */
    @Column(name = "username", length = 64)
    private String username;

    /**
     * 请求方法（GET/POST/PUT/DELETE 等）。
     */
    @Column(name = "method", length = 10)
    private String method;

    /**
     * 请求URI。
     */
    @Column(name = "uri", length = 512)
    private String uri;

    /**
     * 客户端IP地址。
     */
    @Column(name = "ip", length = 128)
    private String ip;

    /**
     * 请求参数（脱敏后）。
     */
    @Column(name = "params", columnDefinition = "TEXT")
    private String params;

    /**
     * 响应状态码。
     */
    @Column(name = "status_code")
    private Integer statusCode;

    /**
     * 请求处理耗时（毫秒）。
     */
    @Column(name = "duration_ms")
    private Integer durationMs;

    /**
     * 错误信息（异常时记录）。
     */
    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    /**
     * 创建时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;
}
