import request from '@/utils/request'

/**
 * 系统管理 API 模块
 */

/**
 * 获取系统概览统计
 */
export function getSystemStats() {
    return request({
        url: '/admin/system/stats',
        method: 'get'
    })
}

/**
 * 获取系统监控信息（CPU、内存、磁盘）
 */
export function getSystemMonitor() {
    return request({
        url: '/admin/system/monitor',
        method: 'get'
    })
}

/**
 * 获取最近系统日志
 * @param {number} limit - 日志条数
 */
export function getRecentLogs(limit = 5) {
    return request({
        url: '/admin/system/logs',
        method: 'get',
        params: { limit }
    })
}

/**
 * 获取系统配置信息
 */
export function getSystemConfig() {
    return request({
        url: '/admin/system/config',
        method: 'get'
    })
}

/**
 * 更新系统配置
 * @param {Object} data - 配置数据
 */
export function updateSystemConfig(data) {
    return request({
        url: '/admin/system/config',
        method: 'put',
        data
    })
}
