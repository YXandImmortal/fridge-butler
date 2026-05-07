package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.ItemCategoryDTO;
import com.yx.fridgebutler.dto.ItemCreateRequest;
import com.yx.fridgebutler.dto.ItemDTO;
import com.yx.fridgebutler.dto.ItemSearchRequest;
import com.yx.fridgebutler.dto.ItemTakeOutRequest;
import com.yx.fridgebutler.dto.ItemUpdateRequest;
import com.yx.fridgebutler.dto.ItemUnitDTO;
import com.yx.fridgebutler.dto.UnitTypeDTO;
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
        return Result.success(itemId);
    }

    /**
     * 更新物品
     */
    @PostMapping("/update")
    public Result<Void> updateItem(@Valid @RequestBody ItemUpdateRequest request) {
        itemService.updateItem(request);
        return Result.success(null);
    }

    /**
     * 查询物品分类列表（系统默认 + 当前用户创建）
     */
    @GetMapping("/category/list")
    public Result<List<ItemCategoryDTO>> listItemCategories() {
        return Result.success(itemService.listItemCategories());
    }

    /**
     * 查询物品单位列表（系统默认 + 当前用户创建）
     */
    @GetMapping("/unit/list")
    public Result<List<ItemUnitDTO>> listItemUnits() {
        return Result.success(itemService.listItemUnits());
    }

    /**
     * 查询单位类型列表（系统默认 + 当前用户创建）
     */
    @GetMapping("/unit-type/list")
    public Result<List<UnitTypeDTO>> listUnitTypes() {
        return Result.success(itemService.listUnitTypes());
    }

    /**
     * 搜索物品（支持关键字模糊搜索、分类筛选、单位类型筛选、数量排序）
     */
    @PostMapping("/search")
    public Result<List<ItemDTO>> searchItems(@Valid @RequestBody ItemSearchRequest request) {
        return Result.success(itemService.searchItems(request));
    }

    /**
     * 删除物品（软删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return Result.success(null);
    }

    /**
     * 取出物品
     */
    @PostMapping("/take-out")
    public Result<Void> takeOutItem(@Valid @RequestBody ItemTakeOutRequest request) {
        itemService.takeOutItem(request);
        return Result.success(null);
    }
}
