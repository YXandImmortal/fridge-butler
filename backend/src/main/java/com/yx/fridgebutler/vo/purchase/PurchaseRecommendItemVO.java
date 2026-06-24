package com.yx.fridgebutler.vo.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * AI 推荐物品项 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRecommendItemVO {

    /** 物品名称。 */
    private String itemName;

    /** 计划数量。 */
    private BigDecimal plannedNum;

    /** 单位ID。 */
    private Long unitId;

    /** 单位名称。 */
    private String unitName;

    /** 单位类型ID。 */
    private Long unitTypeId;

    /** 单位类型名称。 */
    private String unitTypeName;

    /** 分类ID。 */
    private Long categoryId;

    /** 分类名称。 */
    private String categoryName;

    /** 推荐原因。 */
    private String reason;

    /** 是否必需（特殊场景生成时使用）。 */
    private Boolean essential;

    /** 是否建议存入冰箱。 */
    private Boolean storeInFridge;
}
