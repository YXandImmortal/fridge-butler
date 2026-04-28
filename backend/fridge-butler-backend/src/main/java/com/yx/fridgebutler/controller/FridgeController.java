package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.FridgeCreateRequest;
import com.yx.fridgebutler.dto.FridgeDTO;
import com.yx.fridgebutler.dto.FridgeSearchRequest;
import com.yx.fridgebutler.dto.FridgeUpdateRequest;
import com.yx.fridgebutler.service.FridgeService;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/fridge")
public class FridgeController {

    @Autowired
    private FridgeService fridgeService;

    /**
     * 查询当前用户拥有的冰箱列表
     */
    @GetMapping("/list")
    public Result<List<FridgeDTO>> listMyFridges() {
        return Result.success(fridgeService.listMyFridges());
    }

    /**
     * 获取冰箱详情
     */
    @GetMapping("/detail/{id}")
    public Result<FridgeDTO> getFridgeDetail(@PathVariable Long id) {
        return Result.success(fridgeService.getFridgeDetail(id));
    }

    /**
     * 创建冰箱
     */
    @PostMapping("/create")
    public Result<Long> createFridge(@Valid @RequestBody FridgeCreateRequest request) {
        Long fridgeId = fridgeService.createFridge(request);
        return Result.success(fridgeId);
    }

    /**
     * 更新冰箱
     */
    @PatchMapping("/update/{id}")
    public Result<Void> updateFridge(@PathVariable Long id, @Valid @RequestBody FridgeUpdateRequest request) {
        fridgeService.updateFridge(id, request);
        return Result.success(null);
    }

    /**
     * 删除冰箱（软删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteFridge(@PathVariable Long id) {
        fridgeService.deleteFridge(id);
        return Result.success(null);
    }

    /**
     * 在指定冰箱内添加物品（预留接口，暂不支持）
     */
    @PostMapping("/add/{id}/item")
    public Result<Void> addFridgeItem(@PathVariable Long id) {
        log.info("添加冰箱物品接口被调用，冰箱ID：{}，功能暂未实现", id);
        return Result.success("功能暂未实现", null);
    }

    /**
     * 搜索冰箱（优先匹配名称，其次地址，再其次备注）
     */
    @PostMapping("/search")
    public Result<List<FridgeDTO>> searchFridges(@Valid @RequestBody FridgeSearchRequest request) {
        return Result.success(fridgeService.searchFridges(request));
    }

    /**
     * 获取当前用户的默认冰箱
     */
    @GetMapping("/default")
    public Result<FridgeDTO> getDefaultFridge() {
        return Result.success(fridgeService.getDefaultFridge());
    }
}
