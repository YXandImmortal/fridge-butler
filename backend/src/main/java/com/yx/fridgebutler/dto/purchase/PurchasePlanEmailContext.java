package com.yx.fridgebutler.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 采购方案邮件模板上下文。
 * <p>用于向 Thymeleaf 模板传递渲染邮件正文所需的数据。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanEmailContext {

    /** 方案名称。 */
    private String planName;

    /** 创建时间文本（已格式化）。 */
    private String createTime;

    /** 目标冰箱名称。 */
    private String fridgeName;

    /** 场景描述。 */
    private String sceneDesc;

    /** 物品清单。 */
    private List<Item> items;

    /**
     * 邮件中的物品项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        /** 物品名称。 */
        private String itemName;

        /** 计划数量（已格式化，去掉多余小数位）。 */
        private String plannedNum;

        /** 单位名称。 */
        private String itemUnitName;

        /** 分类名称。 */
        private String categoryName;
    }
}
