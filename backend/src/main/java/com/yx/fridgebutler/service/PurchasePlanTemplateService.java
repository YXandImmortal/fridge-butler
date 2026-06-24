package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.purchase.PurchasePlanTemplateCreateRequest;
import com.yx.fridgebutler.dto.purchase.PurchasePlanTemplateUpdateRequest;
import com.yx.fridgebutler.dto.purchase.SavePlanAsTemplateRequest;
import com.yx.fridgebutler.dto.purchase.UseTemplateRequest;
import com.yx.fridgebutler.vo.purchase.PurchasePlanTemplateVO;
import com.yx.fridgebutler.vo.purchase.PurchasePlanVO;

import java.util.List;

/**
 * 用户采购计划模板服务。
 */
public interface PurchasePlanTemplateService {

    /**
     * 从空白创建模板。
     *
     * @param request 创建请求
     * @return 创建的模板
     */
    PurchasePlanTemplateVO createTemplate(PurchasePlanTemplateCreateRequest request);

    /**
     * 将采购计划保存为模板。
     *
     * @param request 保存请求
     * @return 创建的模板
     */
    PurchasePlanTemplateVO savePlanAsTemplate(SavePlanAsTemplateRequest request);

    /**
     * 修改模板。
     *
     * @param id      模板ID
     * @param request 修改请求
     * @return 修改后的模板
     */
    PurchasePlanTemplateVO updateTemplate(Long id, PurchasePlanTemplateUpdateRequest request);

    /**
     * 删除模板。
     *
     * @param id 模板ID
     */
    void deleteTemplate(Long id);

    /**
     * 查询模板详情。
     *
     * @param id 模板ID
     * @return 模板详情
     */
    PurchasePlanTemplateVO getTemplate(Long id);

    /**
     * 查询当前用户的所有模板。
     *
     * @return 模板列表
     */
    List<PurchasePlanTemplateVO> listTemplates();

    /**
     * 使用模板创建采购方案。
     *
     * @param templateId 模板ID
     * @param request    创建请求
     * @return 创建的方案
     */
    PurchasePlanVO useTemplate(Long templateId, UseTemplateRequest request);
}
