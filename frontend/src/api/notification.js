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
 * 获取最新未读重要通知
 * @param {number} [page=1] - 页码
 * @param {number} [size=1] - 每页数量
 */
export function getLatestImportantNotice(params = {}) {
    return request({
        url: '/notification/list',
        method: 'get',
        params: {
            type: 'IMPORTANT_NOTICE',
            status: 0,
            page: 1,
            size: 1,
            ...params
        }
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

// ==================== 管理后台：重要通知广播 ====================

/**
 * 新建并立即广播重要通知
 * @param {Object} data
 * @param {string} data.title - 通知标题
 * @param {string} data.content - Markdown 格式通知内容
 */
export function broadcastImportantNotice(data) {
    return request({
        url: '/admin/notification/broadcast',
        method: 'post',
        data
    })
}

/**
 * 分页查询重要通知模板列表
 * @param {number} [page=1] - 页码
 * @param {number} [size=10] - 每页数量
 */
export function getImportantNoticeList(page = 1, size = 10) {
    return request({
        url: '/admin/notification/important',
        method: 'get',
        params: { page, size }
    })
}

/**
 * 按ID广播/重新广播重要通知
 * @param {number} id - 模板ID
 */
export function broadcastImportantNoticeById(id) {
    return request({
        url: `/admin/notification/important/${id}/broadcast`,
        method: 'post'
    })
}

/**
 * 关闭指定模板广播
 * @param {number} id - 模板ID
 */
export function closeImportantNotice(id) {
    return request({
        url: `/admin/notification/important/${id}/close`,
        method: 'patch'
    })
}
