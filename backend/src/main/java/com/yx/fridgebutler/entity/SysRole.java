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
 * 系统角色实体类。
 * <p>对应数据库表 sys_role，用于存储系统角色信息，如角色名称、角色编码等。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sys_role")
public class SysRole {

    /**
     * 角色ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 角色名称，必填，最大长度20。
     */
    @Size(max = 20)
    @NotNull
    @Column(name = "role_name", nullable = false, length = 20)
    private String roleName;

    /**
     * 角色编码，必填，最大长度20，用于程序中标识角色。
     */
    @Size(max = 20)
    @NotNull
    @Column(name = "role_code", nullable = false, length = 20)
    private String roleCode;

    /**
     * 备注信息，可选，最大长度255，默认值为空字符串。
     */
    @Size(max = 255)
    @ColumnDefault("''")
    @Column(name = "remark")
    private String remark;

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

}
