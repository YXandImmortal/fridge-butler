package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.vo.ItemCategoryVO;
import com.yx.fridgebutler.dto.ItemCreateRequest;
import com.yx.fridgebutler.vo.ItemVO;
import com.yx.fridgebutler.dto.ItemSearchRequest;
import com.yx.fridgebutler.dto.ItemTakeOutRequest;
import com.yx.fridgebutler.dto.ItemUpdateRequest;
import com.yx.fridgebutler.vo.ItemUnitVO;
import com.yx.fridgebutler.vo.UnitTypeVO;
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
}
