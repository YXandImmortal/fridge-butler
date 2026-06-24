import request from '@/utils/request'

/**
 * 采购助手 API 模块
 */

/**
 * 查询采购方案列表
 * GET /purchase/plan
 * @param {Object} params
 */
export function listPurchasePlans(params) {
    return request({
        url: '/purchase/plan',
        method: 'get',
        params
    })
}

/**
 * 查询采购方案详情
 * GET /purchase/plan/{id}
 * @param {number} id
 */
export function getPurchasePlan(id) {
    return request({
        url: `/purchase/plan/${id}`,
        method: 'get'
    })
}

/**
 * 修改采购方案
 * PUT /purchase/plan/{id}
 * @param {number} id
 * @param {Object} data
 */
export function updatePurchasePlan(id, data) {
    return request({
        url: `/purchase/plan/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除采购方案
 * DELETE /purchase/plan/{id}
 * @param {number} id
 */
export function deletePurchasePlan(id) {
    return request({
        url: `/purchase/plan/${id}`,
        method: 'delete'
    })
}

/**
 * 取消采购方案
 * POST /purchase/plan/{id}/cancel
 * @param {number} id
 */
export function cancelPurchasePlan(id) {
    return request({
        url: `/purchase/plan/${id}/cancel`,
        method: 'post'
    })
}

/**
 * 核对入库结算
 * POST /purchase/plan/{id}/settle
 * @param {number} id
 * @param {Object} data
 */
export function settlePurchasePlan(id, data) {
    return request({
        url: `/purchase/plan/${id}/settle`,
        method: 'post',
        data
    })
}

/**
 * 查询当前用户的采购计划模板列表
 * GET /purchase/template
 */
export function listMyTemplates() {
    return request({
        url: '/purchase/template',
        method: 'get'
    })
}

/**
 * 查询特殊场景模板列表
 * GET /purchase/scene-templates
 */
export function listSceneTemplates() {
    return request({
        url: '/purchase/scene-templates',
        method: 'get'
    })
}

/**
 * AI 采购推荐/生成
 * POST /purchase/recommend
 * @param {Object} data
 */
export function purchaseRecommend(data) {
    return request({
        url: '/purchase/recommend',
        method: 'post',
        data
    })
}

/**
 * 使用模板创建采购方案
 * POST /purchase/template/{id}/use
 * @param {number} id
 * @param {Object} data
 */
export function useTemplate(id, data) {
    return request({
        url: `/purchase/template/${id}/use`,
        method: 'post',
        data
    })
}

/**
 * 查询模板详情
 * GET /purchase/template/{id}
 * @param {number} id
 */
export function getTemplateDetail(id) {
    return request({
        url: `/purchase/template/${id}`,
        method: 'get'
    })
}

/**
 * 创建采购方案
 * POST /purchase/plan
 * @param {Object} data
 */
export function createPurchasePlan(data) {
    return request({
        url: '/purchase/plan',
        method: 'post',
        data
    })
}

/**
 * 保存为采购模板
 * POST /purchase/template
 * @param {Object} data
 */
export function createPurchaseTemplate(data) {
    return request({
        url: '/purchase/template',
        method: 'post',
        data
    })
}

/**
 * 删除采购计划模板
 * DELETE /purchase/template/{id}
 * @param {number} id
 */
export function deletePurchaseTemplate(id) {
    return request({
        url: `/purchase/template/${id}`,
        method: 'delete'
    })
}
