package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.FridgeCreateRequest;
import com.yx.fridgebutler.dto.FridgeQueryRequest;
import com.yx.fridgebutler.dto.FridgeDTO;
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
    public Result<List<FridgeDTO>> listMyFridges(@Valid FridgeQueryRequest request) {
        return Result.success(fridgeService.listMyFridges(request));
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
    @PutMapping("/update/{id}")
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
    @GetMapping("/search")
    public Result<List<FridgeDTO>> searchFridges(@RequestParam(required = false) String keyword) {
        return Result.success(fridgeService.searchFridges(keyword));
    }
}
