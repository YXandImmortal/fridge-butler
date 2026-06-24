package com.yx.fridgebutler.vo.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户采购计划模板物品 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanTemplateItemVO {

    /** 物品ID。 */
    private Long id;

    /** 模板ID。 */
    private Long templateId;

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

    /** 排序。 */
    private Integer sortOrder;

    /** 是否建议存入冰箱。 */
    private Boolean storeInFridge;
}
