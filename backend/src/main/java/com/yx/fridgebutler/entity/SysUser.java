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
 * 系统用户实体类。
 * <p>对应数据库表 sys_user，用于存储系统注册用户的信息，包括用户名、密码、手机号、角色、头像等。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class SysUser {

    /**
     * 用户ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户名，必填，唯一，最大长度50。
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码，必填，最大长度60（存储BCrypt加密后的密码）。
     */
    @Size(max = 60)
    @NotNull
    @Column(name = "password", nullable = false, length = 60)
    private String password;

    /**
     * 手机号，唯一，最大长度11。
     */
    @Size(max = 11)
    @Column(name = "mobile", unique = true, length = 11)
    private String mobile;

    /**
     * 角色ID，关联系统角色，必填。
     */
    @NotNull
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /**
     * 创建时间，默认为当前时间戳，必填。
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

    /**
     * 是否删除，true 表示已删除，默认值为 false。
     */
    @ColumnDefault("0")
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    /**
     * 头像标识，必填，最大长度20，默认值为 'ice'。
     */
    @Size(max = 20)
    @NotNull
    @ColumnDefault("'ice'")
    @Column(name = "avatar", nullable = false, length = 20)
    private String avatar;

    /**
     * 是否已完成新手引导，true 表示已完成，默认值为 false，必填。
     */
    @NotNull
    @ColumnDefault("0")
    @Column(name = "guide_completed", nullable = false)
    private Boolean guideCompleted;

    /**
     * 是否已激活，true 表示已激活，默认值为 true。
     * <p>用于密钥激活功能，未激活的普通用户无法使用系统功能。</p>
     */
    @NotNull
    @ColumnDefault("1")
    @Column(name = "is_activated", nullable = false)
    private Boolean isActivated;

    /**
     * 最后登录时间，记录用户最近一次成功登录的时间。
     */
    @Column(name = "last_login_time")
    private Instant lastLoginTime;

    /**
     * 密码最后更新时间。
     */
    @Column(name = "password_updated_at")
    private Instant passwordUpdatedAt;

    /**
     * 电子邮箱，最大长度100。
     */
    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

}
