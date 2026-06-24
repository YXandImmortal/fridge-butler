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
 * 智能推荐物品信息（AI 根据物品名称推荐分类、单位、存放位置等）
 * @param {string} itemName - 物品名称
 * @param {number} fridgeId - 冰箱ID
 */
export function recommendItem(itemName, fridgeId) {
    return request({
        url: '/item/recommend',
        method: 'post',
        data: {itemName, fridgeId}
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
 * @param {string} [data.storageLocation] - 存放位置（可选，最大长度50）
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
 * @param {string} [data.storageLocation] - 存放位置（可选，最大长度50）
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
        method: 'delete'
    })
}

/**
 * 取出物品（减少库存）
 * @param {Object} data - 取出数据
 * @param {number} data.id - 物品ID
 * @param {number} data.takeOutNum - 取出数量
 */
export function takeOutItem(data) {
    return request({
        url: '/item/take-out',
        method: 'post',
        data
    })
}

/**
 * 批量取出物品
 * @param {Object} data - 批量取出数据
 * @param {Array<{id: number, takeOutNum: number}>} data.items - 取出物品列表
 */
export function batchTakeOutItem(data) {
    return request({
        url: '/item/batch/take-out',
        method: 'post',
        data
    })
}

/**
 * 查询物品分类详情
 * @param {number} id - 分类ID
 */
export function getItemCategoryDetail(id) {
    return request({
        url: `/item/category/detail/${id}`,
        method: 'get'
    })
}

/**
 * 创建单位类型
 * @param {Object} data - 单位类型数据
 * @param {string} data.typeName - 单位类型名称
 */
export function createUnitType(data) {
    return request({
        url: '/item/unit-type/create',
        method: 'post',
        data
    })
}

/**
 * 更新单位类型
 * @param {Object} data - 单位类型数据
 * @param {number} data.id - 单位类型ID
 * @param {string} data.typeName - 单位类型名称
 */
export function updateUnitType(data) {
    return request({
        url: '/item/unit-type/update',
        method: 'post',
        data
    })
}

/**
 * 删除单位类型
 * @param {number} id - 单位类型ID
 */
export function deleteUnitType(id) {
    return request({
        url: `/item/unit-type/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 创建物品单位
 * @param {Object} data - 物品单位数据
 * @param {string} data.unitName - 单位名称
 * @param {number} data.unitTypeId - 所属单位类型ID
 */
export function createItemUnit(data) {
    return request({
        url: '/item/unit/create',
        method: 'post',
        data
    })
}

/**
 * 更新物品单位
 * @param {Object} data - 物品单位数据
 * @param {number} data.id - 单位ID
 * @param {string} data.unitName - 单位名称
 * @param {number} data.unitTypeId - 所属单位类型ID
 */
export function updateItemUnit(data) {
    return request({
        url: '/item/unit/update',
        method: 'post',
        data
    })
}

/**
 * 删除物品单位
 * @param {number} id - 单位ID
 */
export function deleteItemUnit(id) {
    return request({
        url: `/item/unit/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 创建物品分类
 * @param {Object} data - 分类数据
 * @param {string} data.categoryName - 分类名称
 */
export function createItemCategory(data) {
    return request({
        url: '/item/category/create',
        method: 'post',
        data
    })
}

/**
 * 更新物品分类
 * @param {Object} data - 分类数据
 * @param {number} data.id - 分类ID
 * @param {string} data.categoryName - 分类名称
 */
export function updateItemCategory(data) {
    return request({
        url: '/item/category/update',
        method: 'post',
        data
    })
}

/**
 * 删除物品分类
 * @param {number} id - 分类ID
 */
export function deleteItemCategory(id) {
    return request({
        url: `/item/category/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 查询近30天取出记录统计
 * @param {number} [fridgeId] - 冰箱ID（可选，不传则统计当前用户所有冰箱）
 */
export function getRecent30DaysTakeOutStats(fridgeId) {
    return request({
        url: '/item/take-out/statistics/recent-30-days',
        method: 'get',
        params: fridgeId ? {fridgeId} : {}
    })
}

/**
 * 查询近30天入库记录统计
 * @param {number} [fridgeId] - 冰箱ID（可选，不传则统计当前用户所有冰箱）
 */
export function getRecent30DaysAddStats(fridgeId) {
    return request({
        url: '/item/add/statistics/recent-30-days',
        method: 'get',
        params: fridgeId ? {fridgeId} : {}
    })
}

/**
 * 查询临期/过期物品汇总统计
 */
export function getExpiringSummary() {
    return request({
        url: '/item/expiring/summary',
        method: 'get'
    })
}
