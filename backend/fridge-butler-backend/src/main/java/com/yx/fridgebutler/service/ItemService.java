package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.ItemCategoryVO;
import com.yx.fridgebutler.dto.ItemCategoryCreateRequest;
import com.yx.fridgebutler.dto.ItemCategoryUpdateRequest;
import com.yx.fridgebutler.dto.ItemCreateRequest;
import com.yx.fridgebutler.vo.ItemVO;
import com.yx.fridgebutler.dto.ItemSearchRequest;
import com.yx.fridgebutler.dto.ItemTakeOutRequest;
import com.yx.fridgebutler.vo.ItemUnitVO;
import com.yx.fridgebutler.dto.ItemUpdateRequest;
import com.yx.fridgebutler.vo.UnitTypeVO;

import java.util.List;

/**
 * 物品服务接口。
 * <p>定义物品的创建、更新、查询、删除，以及物品分类、单位、单位类型的查询和物品取出等业务逻辑。</p>
 */
public interface ItemService {

    /**
     * 创建新物品并存放到指定冰箱中。
     *
     * @param request 物品创建请求参数
     * @return 新创建物品的ID
     */
    Long createItem(ItemCreateRequest request);

    /**
     * 更新指定物品的信息。
     *
     * @param request 物品更新请求参数
     */
    void updateItem(ItemUpdateRequest request);

    /**
     * 按条件搜索物品。
     *
     * @param request 搜索请求参数，支持关键词、分类、单位、冰箱ID等筛选条件
     * @return 符合条件的物品信息列表
     */
    List<ItemVO> searchItems(ItemSearchRequest request);

    /**
     * 查询物品分类列表（系统默认 + 当前用户自定义）。
     *
     * @return 物品分类列表
     */
    List<ItemCategoryVO> listItemCategories();

    /**
     * 查询物品分类详情。
     *
     * @param id 分类ID
     * @return 物品分类详情
     */
    ItemCategoryVO getItemCategory(Long id);

    /**
     * 创建物品分类（用户自定义）。
     *
     * @param request 分类创建请求参数
     * @return 新创建分类的ID
     */
    Long createItemCategory(ItemCategoryCreateRequest request);

    /**
     * 更新物品分类（用户自定义）。
     *
     * @param request 分类更新请求参数
     */
    void updateItemCategory(ItemCategoryUpdateRequest request);

    /**
     * 删除物品分类（用户自定义，软删除）。
     *
     * @param id 分类ID
     */
    void deleteItemCategory(Long id);

    /**
     * 查询物品单位列表（系统默认 + 当前用户自定义）。
     *
     * @return 物品单位列表
     */
    List<ItemUnitVO> listItemUnits();

    /**
     * 查询单位类型列表（系统默认 + 当前用户自定义）。
     *
     * @return 单位类型列表
     */
    List<UnitTypeVO> listUnitTypes();

    /**
     * 删除指定物品（软删除）。
     *
     * @param id 物品ID
     */
    void deleteItem(Long id);

    /**
     * 从冰箱中取出指定数量的物品。
     * <p>若取出后数量归零或负数，则自动软删除该物品。</p>
     *
     * @param request 物品取出请求参数
     */
    void takeOutItem(ItemTakeOutRequest request);
}
