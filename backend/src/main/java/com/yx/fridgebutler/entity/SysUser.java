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
     * 头像标识，必填，最大长度20，默认值为 'bot'。
     */
    @Size(max = 20)
    @NotNull
    @ColumnDefault("'bot'")
    @Column(name = "avatar", nullable = false, length = 20)
    private String avatar;

}
