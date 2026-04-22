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

export function searchFridges(params = '') {
    return request({
        url: '/fridge/search',
        method: 'get',
        params
    })
}

/**
 * 获取冰箱详情
 * @param {number} id - 冰箱ID
 */
export function getFridgeDetail(id) {
    return request({
        url: `/fridge/${id}`,
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
 * 删除冰箱（软删除）
 * @param {number} id - 冰箱ID
 */
export function deleteFridge(id) {
    return request({
        url: `/fridge/delete/${id}`,
        method: 'delete'
    })
}
