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
 * 冰箱类型实体类。
 * <p>对应数据库表 biz_fridge_type，用于存储系统预设的冰箱类型信息
 * （如单门、双门、三门、对开门、多门、迷你冰箱、车载冰箱等）。</p>
 * <p><b>注意：冰箱类型仅限系统预设，不支持用户自定义创建。</b></p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "biz_fridge_type")
public class BizFridgeType {

    /**
     * 类型ID，主键，自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 类型名称，必填，最大长度30。
     * <p>如：单门、双门、三门、对开门、多门、迷你冰箱、车载冰箱等。</p>
     */
    @Size(max = 30)
    @NotNull
    @Column(name = "type_name", nullable = false, length = 30)
    private String typeName;

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
