package com.yx.fridgebutler.vo.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 采购推荐结果 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRecommendVO {

    /** 数据是否充足。 */
    private Boolean sufficientData;

    /** 数据不足原因。 */
    private String insufficientReason;

    /** 提示建议。 */
    private List<String> tips;

    /** 推荐清单。 */
    private List<PurchaseRecommendItemVO> items;
}
