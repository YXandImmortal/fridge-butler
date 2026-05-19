package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.fridge.FridgeCreateRequest;
import com.yx.fridgebutler.vo.FridgeVO;
import com.yx.fridgebutler.dto.fridge.FridgeSearchRequest;
import com.yx.fridgebutler.dto.fridge.FridgeUpdateRequest;
import com.yx.fridgebutler.service.CapacityStatsService;
import com.yx.fridgebutler.service.FridgeService;
import com.yx.fridgebutler.vo.CapacityStatsVO;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 冰箱控制器
 * <p>
 * 处理冰箱的增删改查等管理操作。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/fridge")
public class FridgeController {

    @Autowired
    private FridgeService fridgeService;

    @Autowired
    private CapacityStatsService capacityStatsService;

    /**
     * 查询当前用户拥有的冰箱列表
     */
    @GetMapping("/list")
    public Result<List<FridgeVO>> listMyFridges() {
        List<FridgeVO> result = fridgeService.listMyFridges();
        log.info("查询冰箱列表成功，数量：{}", result.size());
        return Result.success(result);
    }

    /**
     * 获取冰箱详情
     */
    @GetMapping("/detail/{id}")
    public Result<FridgeVO> getFridgeDetail(@PathVariable Long id) {
        FridgeVO result = fridgeService.getFridgeDetail(id);
        log.info("查询冰箱详情成功，冰箱ID：{}，名称：{}", id, result.getFridgeName());
        return Result.success(result);
    }

    /**
     * 创建冰箱
     */
    @PostMapping("/create")
    public Result<Long> createFridge(@Valid @RequestBody FridgeCreateRequest request) {
        Long fridgeId = fridgeService.createFridge(request);
        log.info("创建冰箱成功，冰箱ID：{}，名称：{}", fridgeId, request.getFridgeName());
        return Result.success(fridgeId);
    }

    /**
     * 更新冰箱
     */
    @PatchMapping("/update/{id}")
    public Result<Void> updateFridge(@PathVariable Long id, @Valid @RequestBody FridgeUpdateRequest request) {
        fridgeService.updateFridge(id, request);
        log.info("更新冰箱成功，冰箱ID：{}，名称：{}", id, request.getFridgeName());
        return Result.success(null);
    }

    /**
     * 删除冰箱（软删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteFridge(@PathVariable Long id) {
        fridgeService.deleteFridge(id);
        log.info("删除冰箱成功，冰箱ID：{}", id);
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
    public Result<List<FridgeVO>> searchFridges(@Valid @RequestBody FridgeSearchRequest request) {
        List<FridgeVO> result = fridgeService.searchFridges(request);
        log.info("搜索冰箱成功，关键词：{}，结果数量：{}", request.getKeyword(), result.size());
        return Result.success(result);
    }

    /**
     * 获取当前用户冰箱的容量利用率统计。
     * <p>支持查询所有冰箱或指定单个冰箱。</p>
     *
     * @param fridgeId 指定冰箱ID（可选），为 null 时返回所有冰箱统计
     */
    @GetMapping("/capacity-stats")
    public Result<CapacityStatsVO> getCapacityStats(@RequestParam(required = false) Long fridgeId) {
        CapacityStatsVO result = capacityStatsService.getCapacityStats(fridgeId);
        log.info("查询容量利用率统计成功，冰箱ID：{}，平均占用率：{}%", fridgeId, result.getAvgRate());
        return Result.success(result);
    }

    /**
     * 获取当前用户的默认冰箱
     */
    @GetMapping("/default")
    public Result<FridgeVO> getDefaultFridge() {
        FridgeVO result = fridgeService.getDefaultFridge();
        if (result != null) {
            log.info("查询默认冰箱成功，冰箱ID：{}，名称：{}", result.getId(), result.getFridgeName());
        } else {
            log.info("查询默认冰箱成功，用户未设置默认冰箱");
        }
        return Result.success(result);
    }
}
