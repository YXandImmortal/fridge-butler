package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.purchase.PurchasePlanCreateRequest;
import com.yx.fridgebutler.dto.purchase.PurchasePlanSettleRequest;
import com.yx.fridgebutler.dto.purchase.PurchasePlanUpdateRequest;
import com.yx.fridgebutler.enums.PurchasePlanSource;
import com.yx.fridgebutler.vo.purchase.PurchasePlanSettleResultVO;
import com.yx.fridgebutler.vo.purchase.PurchasePlanVO;

import java.util.List;

/**
 * 采购方案服务。
 */
public interface PurchasePlanService {

    /**
     * 创建采购方案。
     *
     * @param request 创建请求
     * @param source  方案来源
     * @return 创建的方案
     */
    PurchasePlanVO createPlan(PurchasePlanCreateRequest request, PurchasePlanSource source);

    /**
     * 修改采购方案（仅待采购状态可修改）。
     *
     * @param id      方案ID
     * @param request 修改请求
     * @return 修改后的方案
     */
    PurchasePlanVO updatePlan(Long id, PurchasePlanUpdateRequest request);

    /**
     * 删除采购方案。
     *
     * @param id 方案ID
     */
    void deletePlan(Long id);

    /**
     * 取消采购方案。
     *
     * @param id 方案ID
     */
    void cancelPlan(Long id);

    /**
     * 查询方案详情。
     *
     * @param id 方案ID
     * @return 方案详情
     */
    PurchasePlanVO getPlan(Long id);

    /**
     * 查询用户的方案列表。
     *
     * @param planStatus 状态筛选（可选）
     * @return 方案列表
     */
    List<PurchasePlanVO> listPlans(Byte planStatus);

    /**
     * 核对入库并结算。
     *
     * @param planId  方案ID
     * @param request 核对请求
     * @return 结算结果
     */
    PurchasePlanSettleResultVO settle(Long planId, PurchasePlanSettleRequest request);

    /**
     * 将待采购方案发送到当前用户绑定的邮箱。
     *
     * @param id 方案ID
     */
    void sendPlanEmail(Long id);
}
