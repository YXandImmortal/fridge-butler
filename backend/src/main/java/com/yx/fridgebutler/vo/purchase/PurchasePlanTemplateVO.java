package com.yx.fridgebutler.vo.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 用户采购计划模板 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanTemplateVO {

    /** 模板ID。 */
    private Long id;

    /** 模板名称。 */
    private String templateName;

    /** 场景描述。 */
    private String sceneDesc;

    /** 物品数量。 */
    private Integer itemCount;

    /** 创建时间。 */
    private Instant createTime;

    /** 更新时间。 */
    private Instant updateTime;

    /** 物品清单。 */
    private List<PurchasePlanTemplateItemVO> items;
}
