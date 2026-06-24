package com.yx.fridgebutler.dto.item;

import com.yx.fridgebutler.vo.item.ItemRecommendationVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 物品 AI 推荐内部结果 DTO。
 * <p>由 {@link com.yx.fridgebutler.service.ItemIntelligenceService} 返回，
 * 再由 Controller 转换为 {@link ItemRecommendationVO}。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRecommendationResult {

    /**
     * 是否为真实、可放入冰箱的物品。
     */
    private boolean valid;

    /**
     * 标准化后的物品名称。
     */
    private String itemName;

    /**
     * 推荐分类 ID，无合适分类时为 null。
     */
    private Long categoryId;

    /**
     * 推荐分类名称，无合适分类时为 null。
     */
    private String categoryName;

    /**
     * 推荐单位 ID，无合适单位时为 null。
     */
    private Long unitId;

    /**
     * 推荐单位名称，无合适单位时为 null。
     */
    private String unitName;

    /**
     * 推荐单位类型名称，无合适单位时为 null。
     */
    private String unitTypeName;

    /**
     * 推荐存储位置，无推荐时为 null。
     */
    private String storageLocation;

    /**
     * 推荐存放日期，无推荐时默认今天。
     */
    private LocalDate storedDate;

    /**
     * 给前端的提示信息，例如"该物品不适合放入冰箱"。
     */
    private String message;

}
