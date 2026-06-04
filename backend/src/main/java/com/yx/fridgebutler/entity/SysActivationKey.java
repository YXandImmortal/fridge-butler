package com.yx.fridgebutler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * 用户激活密钥实体类。
 * <p>对应数据库表 sys_activation_key，用于存储系统生成的激活密钥及其绑定状态。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sys_activation_key")
public class SysActivationKey {

    /**
     * 密钥ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 密钥字符串，唯一，如 FB-A3F9K2M1。
     */
    @Size(max = 16)
    @NotNull
    @Column(name = "key_code", nullable = false, unique = true, length = 16)
    private String keyCode;

    /**
     * 密钥状态：UNUSED-未使用, BOUND-已绑定, REVOKED-已收回, DESTROYED-已销毁。
     */
    @Size(max = 16)
    @NotNull
    @ColumnDefault("'UNUSED'")
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /**
     * 绑定用户ID，密钥被哪个用户使用。
     */
    @Column(name = "bound_user_id")
    private Long boundUserId;

    /**
     * 绑定时间，密钥被使用的时间。
     */
    @Column(name = "bound_time")
    private Instant boundTime;

    /**
     * 备注信息。
     */
    @Size(max = 255)
    @Column(name = "remark")
    private String remark;

    /**
     * 创建时间，默认为当前时间戳。
     */
    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    /**
     * 更新时间，默认为当前时间戳。
     */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_time")
    private Instant updateTime;
}
