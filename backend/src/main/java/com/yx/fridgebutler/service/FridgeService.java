package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.fridge.FridgeCreateRequest;
import com.yx.fridgebutler.vo.FridgeVO;
import com.yx.fridgebutler.dto.fridge.FridgeSearchRequest;
import com.yx.fridgebutler.dto.fridge.FridgeUpdateRequest;

import java.util.List;

/**
 * 冰箱服务接口。
 * <p>定义冰箱的查询、创建、更新、删除以及默认冰箱管理等业务逻辑。</p>
 */
public interface FridgeService {

    /**
     * 查询当前用户拥有的所有冰箱列表。
     *
     * @return 冰箱信息列表，默认冰箱排在最前面
     */
    List<FridgeVO> listMyFridges();

    /**
     * 获取指定冰箱的详细信息。
     *
     * @param id 冰箱ID
     * @return 冰箱详细信息
     */
    FridgeVO getFridgeDetail(Long id);

    /**
     * 创建新冰箱。
     *
     * @param request 冰箱创建请求参数
     * @return 新创建冰箱的ID
     */
    Long createFridge(FridgeCreateRequest request);

    /**
     * 更新指定冰箱的信息。
     *
     * @param id      冰箱ID
     * @param request 冰箱更新请求参数
     */
    void updateFridge(Long id, FridgeUpdateRequest request);

    /**
     * 删除指定冰箱（软删除）。
     *
     * @param id 冰箱ID
     */
    void deleteFridge(Long id);

    /**
     * 按关键词搜索当前用户的冰箱。
     *
     * @param request 搜索请求参数，包含关键词和排序条件
     * @return 符合条件的冰箱信息列表
     */
    List<FridgeVO> searchFridges(FridgeSearchRequest request);

    /**
     * 获取当前用户的默认冰箱。
     *
     * @return 默认冰箱信息，未设置则返回 null
     */
    FridgeVO getDefaultFridge();
}
