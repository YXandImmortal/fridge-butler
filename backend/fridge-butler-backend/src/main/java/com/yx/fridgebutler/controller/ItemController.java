package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.vo.ItemCategoryVO;
import com.yx.fridgebutler.dto.ItemCategoryCreateRequest;
import com.yx.fridgebutler.dto.ItemCategoryUpdateRequest;
import com.yx.fridgebutler.dto.ItemCreateRequest;
import com.yx.fridgebutler.vo.ItemVO;
import com.yx.fridgebutler.dto.ItemSearchRequest;
import com.yx.fridgebutler.dto.ItemTakeOutRequest;
import com.yx.fridgebutler.dto.ItemUnitCreateRequest;
import com.yx.fridgebutler.dto.ItemUnitUpdateRequest;
import com.yx.fridgebutler.dto.ItemUpdateRequest;
import com.yx.fridgebutler.vo.TakeOutDailyStatisticsVO;
import com.yx.fridgebutler.vo.ItemUnitVO;
import com.yx.fridgebutler.vo.UnitTypeVO;
import com.yx.fridgebutler.dto.UnitTypeCreateRequest;
import com.yx.fridgebutler.dto.UnitTypeUpdateRequest;
import com.yx.fridgebutler.service.ItemService;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 物品控制器
 * <p>
 * 处理物品的增删改查、分类管理、单位管理等操作。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 新增物品
     */
    @PostMapping("/create")
    public Result<Long> createItem(@Valid @RequestBody ItemCreateRequest request) {
        Long itemId = itemService.createItem(request);
        log.info("新增物品成功，物品ID：{}，名称：{}，冰箱ID：{}", itemId, request.getItemName(), request.getFridgeId());
        return Result.success(itemId);
    }

    /**
     * 更新物品
     */
    @PostMapping("/update")
    public Result<Void> updateItem(@Valid @RequestBody ItemUpdateRequest request) {
        itemService.updateItem(request);
        log.info("更新物品成功，物品ID：{}，名称：{}", request.getId(), request.getItemName());
        return Result.success(null);
    }

    /**
     * 查询物品分类列表（系统默认 + 当前用户创建）
     */
    @GetMapping("/category/list")
    public Result<List<ItemCategoryVO>> listItemCategories() {
        List<ItemCategoryVO> result = itemService.listItemCategories();
        log.info("查询物品分类列表成功，数量：{}", result.size());
        return Result.success(result);
    }

    /**
     * 查询物品分类详情
     */
    @GetMapping("/category/detail/{id}")
    public Result<ItemCategoryVO> getItemCategory(@PathVariable Long id) {
        ItemCategoryVO result = itemService.getItemCategory(id);
        log.info("查询物品分类详情成功，分类ID：{}，名称：{}", id, result.getCategoryName());
        return Result.success(result);
    }

    /**
     * 创建物品分类（用户自定义）
     */
    @PostMapping("/category/create")
    public Result<Long> createItemCategory(@Valid @RequestBody ItemCategoryCreateRequest request) {
        Long categoryId = itemService.createItemCategory(request);
        log.info("创建物品分类成功，分类ID：{}，名称：{}", categoryId, request.getCategoryName());
        return Result.success(categoryId);
    }

    /**
     * 更新物品分类（用户自定义）
     */
    @PostMapping("/category/update")
    public Result<Void> updateItemCategory(@Valid @RequestBody ItemCategoryUpdateRequest request) {
        itemService.updateItemCategory(request);
        log.info("更新物品分类成功，分类ID：{}，名称：{}", request.getId(), request.getCategoryName());
        return Result.success(null);
    }

    /**
     * 删除物品分类（用户自定义，软删除）
     */
    @DeleteMapping("/category/delete/{id}")
    public Result<Void> deleteItemCategory(@PathVariable Long id) {
        itemService.deleteItemCategory(id);
        log.info("删除物品分类成功，分类ID：{}", id);
        return Result.success(null);
    }

    /**
     * 查询物品单位列表（系统默认 + 当前用户创建）
     */
    @GetMapping("/unit/list")
    public Result<List<ItemUnitVO>> listItemUnits() {
        List<ItemUnitVO> result = itemService.listItemUnits();
        log.info("查询物品单位列表成功，数量：{}", result.size());
        return Result.success(result);
    }

    /**
     * 查询单位类型列表（系统默认 + 当前用户创建）
     */
    @GetMapping("/unit-type/list")
    public Result<List<UnitTypeVO>> listUnitTypes() {
        List<UnitTypeVO> result = itemService.listUnitTypes();
        log.info("查询单位类型列表成功，数量：{}", result.size());
        return Result.success(result);
    }

    /**
     * 创建单位类型（用户自定义）
     */
    @PostMapping("/unit-type/create")
    public Result<Long> createUnitType(@Valid @RequestBody UnitTypeCreateRequest request) {
        Long unitTypeId = itemService.createUnitType(request);
        log.info("创建单位类型成功，类型ID：{}，名称：{}", unitTypeId, request.getTypeName());
        return Result.success(unitTypeId);
    }

    /**
     * 更新单位类型（用户自定义）
     */
    @PostMapping("/unit-type/update")
    public Result<Void> updateUnitType(@Valid @RequestBody UnitTypeUpdateRequest request) {
        itemService.updateUnitType(request);
        log.info("更新单位类型成功，类型ID：{}，名称：{}", request.getId(), request.getTypeName());
        return Result.success(null);
    }

    /**
     * 删除单位类型（用户自定义，软删除）
     */
    @DeleteMapping("/unit-type/delete/{id}")
    public Result<Void> deleteUnitType(@PathVariable Long id) {
        itemService.deleteUnitType(id);
        log.info("删除单位类型成功，类型ID：{}", id);
        return Result.success(null);
    }

    /**
     * 创建物品单位（用户自定义）
     */
    @PostMapping("/unit/create")
    public Result<Long> createItemUnit(@Valid @RequestBody ItemUnitCreateRequest request) {
        Long unitId = itemService.createItemUnit(request);
        log.info("创建物品单位成功，单位ID：{}，名称：{}", unitId, request.getUnitName());
        return Result.success(unitId);
    }

    /**
     * 更新物品单位（用户自定义）
     */
    @PostMapping("/unit/update")
    public Result<Void> updateItemUnit(@Valid @RequestBody ItemUnitUpdateRequest request) {
        itemService.updateItemUnit(request);
        log.info("更新物品单位成功，单位ID：{}，名称：{}", request.getId(), request.getUnitName());
        return Result.success(null);
    }

    /**
     * 删除物品单位（用户自定义，软删除）
     */
    @DeleteMapping("/unit/delete/{id}")
    public Result<Void> deleteItemUnit(@PathVariable Long id) {
        itemService.deleteItemUnit(id);
        log.info("删除物品单位成功，单位ID：{}", id);
        return Result.success(null);
    }

    /**
     * 搜索物品（支持关键字模糊搜索、分类筛选、单位类型筛选、数量排序）
     */
    @PostMapping("/search")
    public Result<List<ItemVO>> searchItems(@Valid @RequestBody ItemSearchRequest request) {
        List<ItemVO> result = itemService.searchItems(request);
        log.info("搜索物品成功，冰箱ID：{}，关键词：{}，结果数量：{}",
                request.getFridgeId(), request.getKeyword(), result.size());
        return Result.success(result);
    }

    /**
     * 删除物品（软删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        log.info("删除物品成功，物品ID：{}", id);
        return Result.success(null);
    }

    /**
     * 取出物品
     */
    @PostMapping("/take-out")
    public Result<Void> takeOutItem(@Valid @RequestBody ItemTakeOutRequest request) {
        itemService.takeOutItem(request);
        log.info("取出物品成功，物品ID：{}，取出数量：{}", request.getId(), request.getTakeOutNum());
        return Result.success(null);
    }

    /**
     * 查询近30天每日取出次数统计
     * <p>返回包含今天在内的近30天数据，按日期升序排列，无取出记录的日期次数为0。</p>
     *
     * @param fridgeId 冰箱ID（可选，不传则统计当前用户所有冰箱）
     * @return 近30天每日取出次数列表
     */
    @GetMapping("/take-out/statistics/recent-30-days")
    public Result<List<TakeOutDailyStatisticsVO>> getRecent30DaysTakeOutStatistics(
            @RequestParam(required = false) Long fridgeId) {
        List<TakeOutDailyStatisticsVO> result = itemService.getRecent30DaysTakeOutStatistics(fridgeId);
        log.info("查询近30天取出统计成功，冰箱ID：{}，数据条数：{}", fridgeId, result.size());
        return Result.success(result);
    }

    /**
     * 查询近30天每日添加物品次数统计
     * <p>返回包含今天在内的近30天数据，按日期升序排列，无添加记录的日期次数为0。</p>
     *
     * @param fridgeId 冰箱ID（可选，不传则统计当前用户所有冰箱）
     * @return 近30天每日添加次数列表
     */
    @GetMapping("/add/statistics/recent-30-days")
    public Result<List<TakeOutDailyStatisticsVO>> getRecent30DaysAddStatistics(
            @RequestParam(required = false) Long fridgeId) {
        List<TakeOutDailyStatisticsVO> result = itemService.getRecent30DaysAddStatistics(fridgeId);
        log.info("查询近30天添加统计成功，冰箱ID：{}，数据条数：{}", fridgeId, result.size());
        return Result.success(result);
    }
}
