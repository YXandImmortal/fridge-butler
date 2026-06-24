package com.yx.fridgebutler.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 存放位置推荐请求项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageLocationSuggestRequest {

    /** 物品名称。 */
    private String itemName;

    /** 分类名称。 */
    private String categoryName;

    /** 保质期天数。 */
    private Integer shelfLifeDays;
}
