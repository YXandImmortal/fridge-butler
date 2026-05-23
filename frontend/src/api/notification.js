import request from '@/utils/request'

/**
 * 消息提醒模块 API
 */

/**
 * 查询消息列表（支持类型筛选、分页）
 * @param {Object} params
 * @param {string} [params.type] - 类型筛选
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.size=20] - 每页数量
 */
export function getNotificationList(params = {}) {
    return request({
        url: '/notification/list',
        method: 'get',
        params: {
            page: 1,
            size: 20,
            ...params
        }
    })
}

/**
 * 获取未读消息总数
 */
export function getUnreadCount() {
    return request({
        url: '/notification/unread-count',
        method: 'get'
    })
}

/**
 * 获取各类型未读统计摘要
 */
export function getNotificationSummary() {
    return request({
        url: '/notification/summary',
        method: 'get'
    })
}

/**
 * 标记单条消息已读
 * @param {number} id - 消息ID
 */
export function markAsRead(id) {
    return request({
        url: `/notification/read/${id}`,
        method: 'patch'
    })
}

/**
 * 一键标记全部已读
 */
export function markAllAsRead() {
    return request({
        url: '/notification/read-all',
        method: 'patch'
    })
}

/**
 * 删除单条消息（软删除）
 * @param {number} id - 消息ID
 */
export function deleteNotification(id) {
    return request({
        url: `/notification/${id}`,
        method: 'delete'
    })
}
