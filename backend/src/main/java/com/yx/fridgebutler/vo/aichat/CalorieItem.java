package com.yx.fridgebutler.vo.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热量计算中的食材明细项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalorieItem {

    /** 食材名称 */
    private String name;

    /** 份量描述，如 "2个" / "200g" / "1碗" */
    private String amount;

    /** 该食材的热量数值 */
    private Integer calories;

    /** 食材图标 emoji（可选），如 "🥚" "🍅" */
    private String icon;
}
