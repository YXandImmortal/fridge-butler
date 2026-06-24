package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.purchase.*;
import com.yx.fridgebutler.enums.PurchasePlanSource;
import com.yx.fridgebutler.service.PurchaseIntelligenceService;
import com.yx.fridgebutler.service.PurchasePlanService;
import com.yx.fridgebutler.service.PurchasePlanTemplateService;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.purchase.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 采购助手控制器。
 * <p>统一处理采购方案、AI 推荐、用户模板相关接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/purchase")
public class PurchasePlanController {

    @Autowired
    private PurchasePlanService purchasePlanService;

    @Autowired
    private PurchasePlanTemplateService purchasePlanTemplateService;

    @Autowired
    private PurchaseIntelligenceService purchaseIntelligenceService;

    // ======================== 采购方案 ========================

    /**
     * 创建采购方案（手动创建）。
     */
    @PostMapping("/plan")
    public Result<PurchasePlanVO> createPlan(@Valid @RequestBody PurchasePlanCreateRequest request) {
        PurchasePlanVO result = purchasePlanService.createPlan(request, PurchasePlanSource.MANUAL_CREATE);
        log.info("创建采购方案成功，方案ID：{}，用户ID：{}", result.getId(), result.getFridgeId());
        return Result.success(result);
    }

    /**
     * 查询用户的采购方案列表。
     */
    @GetMapping("/plan")
    public Result<List<PurchasePlanVO>> listPlans(
            @RequestParam(name = "planStatus", required = false) Byte planStatus) {
        List<PurchasePlanVO> result = purchasePlanService.listPlans(planStatus);
        return Result.success(result);
    }

    /**
     * 查询采购方案详情。
     */
    @GetMapping("/plan/{id}")
    public Result<PurchasePlanVO> getPlan(@PathVariable("id") Long id) {
        PurchasePlanVO result = purchasePlanService.getPlan(id);
        return Result.success(result);
    }

    /**
     * 修改采购方案。
     */
    @PutMapping("/plan/{id}")
    public Result<PurchasePlanVO> updatePlan(@PathVariable("id") Long id,
                                             @Valid @RequestBody PurchasePlanUpdateRequest request) {
        PurchasePlanVO result = purchasePlanService.updatePlan(id, request);
        return Result.success(result);
    }

    /**
     * 删除采购方案。
     */
    @DeleteMapping("/plan/{id}")
    public Result<Void> deletePlan(@PathVariable("id") Long id) {
        purchasePlanService.deletePlan(id);
        return Result.success(null);
    }

    /**
     * 取消采购方案。
     */
    @PostMapping("/plan/{id}/cancel")
    public Result<Void> cancelPlan(@PathVariable("id") Long id) {
        purchasePlanService.cancelPlan(id);
        return Result.success(null);
    }

    /**
     * 采购方案核对入库结算。
     */
    @PostMapping("/plan/{id}/settle")
    public Result<PurchasePlanSettleResultVO> settlePlan(@PathVariable("id") Long id,
                                                         @Valid @RequestBody PurchasePlanSettleRequest request) {
        PurchasePlanSettleResultVO result = purchasePlanService.settle(id, request);
        log.info("采购方案入库结算成功，方案ID：{}，入库：{}，不入库：{}，跳过：{}",
                id, result.getSettledCount(), result.getNotStoredCount(), result.getSkippedCount());
        return Result.success(result);
    }

    /**
     * 将待采购方案发送到当前用户绑定的邮箱。
     */
    @PostMapping("/plan/{id}/send-email")
    public Result<Void> sendPlanEmail(@PathVariable("id") Long id) {
        purchasePlanService.sendPlanEmail(id);
        log.info("采购方案邮件发送请求已提交，方案ID：{}", id);
        return Result.success(null);
    }

    // ======================== AI 推荐 ========================

    /**
     * AI 采购推荐/生成。
     */
    @PostMapping("/recommend")
    public Result<PurchaseRecommendVO> recommend(@Valid @RequestBody PurchaseRecommendRequest request) {
        PurchaseRecommendVO result = purchaseIntelligenceService.recommend(request);
        log.info("AI 采购推荐完成，冰箱ID：{}，模式：{}，数据充足：{}，推荐项数：{}",
                request.getFridgeId(), request.getMode(), result.getSufficientData(),
                result.getItems() != null ? result.getItems().size() : 0);
        return Result.success(result);
    }

    /**
     * 查询特殊场景提示词模板列表。
     */
    @GetMapping("/scene-templates")
    public Result<List<SceneTemplateVO>> listSceneTemplates() {
        List<SceneTemplateVO> result = purchaseIntelligenceService.listSceneTemplates();
        return Result.success(result);
    }

    // ======================== 用户采购计划模板 ========================

    /**
     * 查询当前用户的采购计划模板列表。
     */
    @GetMapping("/template")
    public Result<List<PurchasePlanTemplateVO>> listTemplates() {
        List<PurchasePlanTemplateVO> result = purchasePlanTemplateService.listTemplates();
        return Result.success(result);
    }

    /**
     * 创建采购计划模板（从空白）。
     */
    @PostMapping("/template")
    public Result<PurchasePlanTemplateVO> createTemplate(
            @Valid @RequestBody PurchasePlanTemplateCreateRequest request) {
        PurchasePlanTemplateVO result = purchasePlanTemplateService.createTemplate(request);
        return Result.success(result);
    }

    /**
     * 将采购方案保存为模板。
     */
    @PostMapping("/template/from-plan")
    public Result<PurchasePlanTemplateVO> savePlanAsTemplate(
            @Valid @RequestBody SavePlanAsTemplateRequest request) {
        PurchasePlanTemplateVO result = purchasePlanTemplateService.savePlanAsTemplate(request);
        return Result.success(result);
    }

    /**
     * 查询采购计划模板详情。
     */
    @GetMapping("/template/{id}")
    public Result<PurchasePlanTemplateVO> getTemplate(@PathVariable("id") Long id) {
        PurchasePlanTemplateVO result = purchasePlanTemplateService.getTemplate(id);
        return Result.success(result);
    }

    /**
     * 修改采购计划模板。
     */
    @PutMapping("/template/{id}")
    public Result<PurchasePlanTemplateVO> updateTemplate(
            @PathVariable("id") Long id,
            @Valid @RequestBody PurchasePlanTemplateUpdateRequest request) {
        PurchasePlanTemplateVO result = purchasePlanTemplateService.updateTemplate(id, request);
        return Result.success(result);
    }

    /**
     * 删除采购计划模板。
     */
    @DeleteMapping("/template/{id}")
    public Result<Void> deleteTemplate(@PathVariable("id") Long id) {
        purchasePlanTemplateService.deleteTemplate(id);
        return Result.success(null);
    }

    /**
     * 使用模板创建采购方案。
     */
    @PostMapping("/template/{id}/use")
    public Result<PurchasePlanVO> useTemplate(@PathVariable("id") Long id,
                                              @Valid @RequestBody UseTemplateRequest request) {
        PurchasePlanVO result = purchasePlanTemplateService.useTemplate(id, request);
        log.info("使用模板创建采购方案成功，模板ID：{}，方案ID：{}", id, result.getId());
        return Result.success(result);
    }
}
