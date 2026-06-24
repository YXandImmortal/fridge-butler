package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.purchase.PurchaseRecommendRequest;
import com.yx.fridgebutler.dto.purchase.StorageLocationSuggestRequest;
import com.yx.fridgebutler.vo.purchase.PurchaseRecommendVO;
import com.yx.fridgebutler.vo.purchase.SceneTemplateVO;

import java.util.List;

/**
 * 采购智能推荐服务。
 * <p>基于 AI 能力，为用户提供日常采购推荐和特殊场景采购清单生成。</p>
 */
public interface PurchaseIntelligenceService {

    /**
     * 根据模式进行 AI 推荐或生成。
     *
     * @param request 推荐请求
     * @return 推荐结果
     */
    PurchaseRecommendVO recommend(PurchaseRecommendRequest request);

    /**
     * 查询特殊场景提示词模板列表。
     *
     * @return 场景模板列表
     */
    List<SceneTemplateVO> listSceneTemplates();

    /**
     * 批量推荐物品存放位置。
     *
     * @param items 待推荐物品列表
     * @return 与输入顺序对应的存放位置列表；失败或无法推荐时对应位置可能为 null
     */
    List<String> suggestStorageLocations(List<StorageLocationSuggestRequest> items);
}
