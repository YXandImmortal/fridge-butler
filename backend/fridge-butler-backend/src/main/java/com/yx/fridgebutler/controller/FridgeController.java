package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.FridgeCreateRequest;
import com.yx.fridgebutler.dto.FridgeQueryRequest;
import com.yx.fridgebutler.dto.FridgeDTO;
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
    @GetMapping("/{id}")
    public Result<FridgeDTO> getFridgeDetail(@PathVariable Long id) {
        return Result.success(fridgeService.getFridgeDetail(id));
    }

    /**
     * 创建冰箱
     */
    @PostMapping
    public Result<Long> createFridge(@Valid @RequestBody FridgeCreateRequest request) {
        Long fridgeId = fridgeService.createFridge(request);
        return Result.success(fridgeId);
    }

    /**
     * 删除冰箱（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFridge(@PathVariable Long id) {
        fridgeService.deleteFridge(id);
        return Result.success(null);
    }

    /**
     * 在指定冰箱内添加物品（预留接口，暂不支持）
     */
    @PostMapping("/{id}/item")
    public Result<Void> addFridgeItem(@PathVariable Long id) {
        log.info("添加冰箱物品接口被调用，冰箱ID：{}，功能暂未实现", id);
        return Result.success("功能暂未实现", null);
    }
}
