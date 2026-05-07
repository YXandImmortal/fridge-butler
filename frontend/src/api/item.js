import request from '@/utils/request'

/**
 * 物品管理 API 模块
 */

/**
 * 查询物品分类列表（系统默认 + 当前用户创建）
 */
export function listItemCategories() {
    return request({
        url: '/item/category/list',
        method: 'get'
    })
}

/**
 * 查询物品单位列表（系统默认 + 当前用户创建）
 */
export function listItemUnits() {
    return request({
        url: '/item/unit/list',
        method: 'get'
    })
}

/**
 * 查询单位类型列表（系统默认 + 当前用户创建）
 */
export function listUnitTypes() {
    return request({
        url: '/item/unit-type/list',
        method: 'get'
    })
}

/**
 * 搜索物品（支持关键字模糊搜索、分类/单位/冰箱筛选、排序）
 * @param {Object} params - 搜索参数
 * @param {string} [params.keyword] - 搜索关键字（模糊匹配物品名称）
 * @param {number} [params.categoryId] - 分类ID筛选
 * @param {number} [params.unitId] - 单位ID筛选
 * @param {number} [params.fridgeId] - 冰箱ID筛选
 * @param {string} [params.sortField] - 排序字段：itemNum（数量）、createTime（创建时间）、storedDate（入库时间）
 * @param {string} [params.sortOrder] - 排序方向：asc（升序）、desc（降序）
 */
export function searchItems(params = {}) {
    return request({
        url: '/item/search',
        method: 'post',
        data: params
    })
}

/**
 * 创建物品
 * @param {Object} data - 物品数据
 * @param {string} data.itemName - 物品名称
 * @param {number} data.categoryId - 分类ID
 * @param {number} data.itemNum - 数量
 * @param {number} data.itemUnitId - 单位ID
 * @param {number} data.fridgeId - 冰箱ID
 */
export function createItem(data) {
    return request({
        url: '/item/create',
        method: 'post',
        data
    })
}

/**
 * 更新物品
 * @param {Object} data - 物品数据
 * @param {number} data.id - 物品ID
 * @param {string} [data.itemName] - 物品名称
 * @param {number} [data.categoryId] - 分类ID
 * @param {number} [data.itemNum] - 数量
 * @param {number} [data.itemUnitId] - 单位ID
 */
export function updateItem(data) {
    return request({
        url: '/item/update',
        method: 'post',
        data
    })
}

/**
 * 删除物品
 * @param {number} id - 物品ID
 */
export function deleteItem(id) {
    return request({
        url: `/item/delete/${id}`,
        method: 'post'
    })
}

/**
 * 取出物品（减少库存）
 * @param {Object} data - 取出数据
 * @param {number} data.itemId - 物品ID
 * @param {number} data.takeOutNum - 取出数量
 */
export function takeOutItem(data) {
    return request({
        url: '/item/take-out',
        method: 'post',
        data
    })
}
