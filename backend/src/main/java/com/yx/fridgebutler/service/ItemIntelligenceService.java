package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.item.ItemRecommendationResult;

/**
 * 物品智能推荐服务。
 * <p>基于 AI 能力，根据用户输入的物品名称推荐分类、单位、存储位置、存放日期等字段。</p>
 */
public interface ItemIntelligenceService {

    /**
     * 根据物品名称推荐相关字段。
     *
     * @param itemName 物品名称
     * @param fridgeId 冰箱 ID，可为 null
     * @return 推荐结果
     */
    ItemRecommendationResult recommend(String itemName, Long fridgeId);

}
