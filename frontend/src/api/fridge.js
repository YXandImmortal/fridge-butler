import request from '@/utils/request'

/**
 * 冰箱管理 API 模块
 */

/**
 * 查询当前用户拥有的冰箱列表
 */
export function listMyFridges() {
    return request({
        url: '/fridge/list',
        method: 'get'
    })
}

export function searchFridges(params = {}) {
    console.log(params)
    return request({
        url: '/fridge/search',
        method: 'post',
        data: params
    })
}

/**
 * 获取冰箱详情
 * @param {number} id - 冰箱ID
 */
export function getFridgeDetail(id) {
    return request({
        url: `/fridge/detail/${id}`,
        method: 'get'
    })
}

/**
 * 创建冰箱
 * @param {Object} data - 创建参数
 * @param {string} data.name - 冰箱名称
 * @param {string} [data.description] - 冰箱描述
 */
export function createFridge(data) {
    return request({
        url: '/fridge/create',
        method: 'post',
        data
    })
}

/**
 * 更新冰箱信息
 * @param {Object} data - 更新参数
 * @param {number} data.id - 冰箱ID
 * @param {string} data.fridgeName - 冰箱名称
 * @param {boolean} data.isDefault - 是否为默认冰箱
 * @param {string} [data.fridgeAddress] - 地址
 * @param {string} [data.remark] - 备注
 * @param {number} [data.totalCapacity] - 总容量
 * @param {boolean} [data.status] - 状态
 */
export function updateFridge(data) {
    return request({
        url: `/fridge/update/${data.id}`,
        method: 'patch',
        data
    })
}

/**
 * 获取当前用户的默认冰箱
 */
export function getDefaultFridge() {
    return request({
        url: '/fridge/default',
        method: 'get'
    })
}

/**
 * 删除冰箱（软删除）
 * @param {number} id - 冰箱ID
 */
export function deleteFridge(id) {
    return request({
        url: `/fridge/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 获取冰箱容量利用率统计
 * 包含总体平均利用率及各冰箱利用率明细
 * @param {number} [fridgeId] - 指定冰箱ID，不传则返回当前用户所有冰箱
 */
export function getCapacityStats(fridgeId = null) {
    return request({
        url: '/fridge/capacity-stats',
        method: 'get',
        params: fridgeId ? {fridgeId} : {}
    })
}
