package com.yx.fridgebutler.vo.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购方案物品 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanItemVO {

    /** 物品ID。 */
    private Long id;

    /** 方案ID。 */
    private Long planId;

    /** 物品名称。 */
    private String itemName;

    /** 分类ID。 */
    private Long categoryId;

    /** 分类名称。 */
    private String categoryName;

    /** 计划数量。 */
    private BigDecimal plannedNum;

    /** 单位ID。 */
    private Long itemUnitId;

    /** 单位名称。 */
    private String itemUnitName;

    /** 单位类型ID。 */
    private Long unitTypeId;

    /** 单位类型名称。 */
    private String unitTypeName;

    /** 实际数量。 */
    private BigDecimal actualNum;

    /** 生产日期。 */
    private LocalDate productionDate;

    /** 保质期天数。 */
    private Integer shelfLifeDays;

    /** 存放位置。 */
    private String storageLocation;

    /** 状态：1=待采购, 2=已入库, 3=跳过, 4=已采购不入库。 */
    private Byte status;

    /** 状态描述。 */
    private String statusDesc;

    /** 是否建议存入冰箱。 */
    private Boolean storeInFridge;

    /** 备注。 */
    private String remark;
}
