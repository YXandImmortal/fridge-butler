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
 * 物品分类实体类。
 * <p>对应数据库表 biz_item_category，用于存储物品的分类信息，支持系统默认分类和用户自定义分类。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_item_category")
public class BizItemCategory {

    /**
     * 分类ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 分类名称，必填，最大长度30。
     */
    @Size(max = 30)
    @NotNull
    @Column(name = "category_name", nullable = false, length = 30)
    private String categoryName;

    /**
     * 所有者ID，关联系统用户，系统默认分类时为空。
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * 是否系统默认分类，true 表示系统默认，默认值为 true。
     */
    @ColumnDefault("1")
    @Column(name = "is_system_default")
    private Boolean isSystemDefault;

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

    /**
     * 是否删除，true 表示已删除，默认值为 false。
     */
    @ColumnDefault("0")
    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
