package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.ItemCategoryVO;
import com.yx.fridgebutler.dto.category.ItemCategoryCreateRequest;
import com.yx.fridgebutler.dto.category.ItemCategoryUpdateRequest;
import com.yx.fridgebutler.dto.item.ItemCreateRequest;
import com.yx.fridgebutler.vo.ItemVO;
import com.yx.fridgebutler.dto.item.ItemSearchRequest;
import com.yx.fridgebutler.dto.item.ItemTakeOutRequest;
import com.yx.fridgebutler.vo.ItemUnitVO;
import com.yx.fridgebutler.dto.unit.ItemUnitCreateRequest;
import com.yx.fridgebutler.dto.unit.ItemUnitUpdateRequest;
import com.yx.fridgebutler.dto.item.ItemUpdateRequest;
import com.yx.fridgebutler.vo.ExpiringSummaryVO;
import com.yx.fridgebutler.vo.TakeOutDailyStatisticsVO;
import com.yx.fridgebutler.vo.UnitTypeVO;
import com.yx.fridgebutler.dto.unittype.UnitTypeCreateRequest;
import com.yx.fridgebutler.dto.unittype.UnitTypeUpdateRequest;

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
     * 创建单位类型（用户自定义）。
     *
     * @param request 单位类型创建请求参数
     * @return 新创建单位类型的ID
     */
    Long createUnitType(UnitTypeCreateRequest request);

    /**
     * 更新单位类型（用户自定义）。
     *
     * @param request 单位类型更新请求参数
     */
    void updateUnitType(UnitTypeUpdateRequest request);

    /**
     * 删除单位类型（用户自定义，软删除）。
     *
     * @param id 单位类型ID
     */
    void deleteUnitType(Long id);

    /**
     * 创建物品单位（用户自定义）。
     *
     * @param request 物品单位创建请求参数
     * @return 新创建物品单位的ID
     */
    Long createItemUnit(ItemUnitCreateRequest request);

    /**
     * 更新物品单位（用户自定义）。
     *
     * @param request 物品单位更新请求参数
     */
    void updateItemUnit(ItemUnitUpdateRequest request);

    /**
     * 删除物品单位（用户自定义，软删除）。
     *
     * @param id 物品单位ID
     */
    void deleteItemUnit(Long id);

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

    /**
     * 查询近30天每日取出次数统计。
     * <p>返回包含今天在内的近30天数据，无取出记录的日期次数为0。</p>
     *
     * @param fridgeId 冰箱ID（为null时统计当前用户所有冰箱）
     * @return 近30天每日取出次数列表，按日期升序排列
     */
    List<TakeOutDailyStatisticsVO> getRecent30DaysTakeOutStatistics(Long fridgeId);

    /**
     * 查询近30天每日添加物品次数统计。
     * <p>返回包含今天在内的近30天数据，无添加记录的日期次数为0。</p>
     *
     * @param fridgeId 冰箱ID（为null时统计当前用户所有冰箱）
     * @return 近30天每日添加次数列表，按日期升序排列
     */
    List<TakeOutDailyStatisticsVO> getRecent30DaysAddStatistics(Long fridgeId);

    /**
     * 查询当前用户临期/过期物品统计摘要。
     * <p>算法与前端 getFreshnessStatus 保持一致：
     * <ul>
     *   <li>保质期 &gt; 30 天的物品视为长保质期，不参与统计</li>
     *   <li>缺少生产日期或保质期的物品跳过</li>
     *   <li>R = (remainingDays / shelfLifeDays) × 100</li>
     *   <li>R ≤ 0 → 已过期；0 &lt; R &lt; 20 → 临期</li>
     * </ul>
     * </p>
     *
     * @return 临期统计摘要，包含临期数、过期数、总计数
     */
    ExpiringSummaryVO getExpiringSummary();
}
