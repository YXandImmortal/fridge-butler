package com.yx.fridgebutler.vo.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物品信息响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVO {

    /**
     * 物品ID
     */
    private Long id;

    /**
     * 冰箱ID
     */
    private Long fridgeId;

    /**
     * 物品名称
     */
    private String itemName;

    /**
     * 单位ID
     */
    private Long itemUnitId;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 单位类型ID
     */
    private Long unitTypeId;

    /**
     * 单位类型名称
     */
    private String unitTypeName;

    /**
     * 入库日期
     */
    private LocalDate storedDate;

    /**
     * 生产日期
     */
    private LocalDate productionDate;

    /**
     * 保质期天数
     */
    private Integer shelfLifeDays;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 数量
     */
    private BigDecimal itemNum;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;
}