import request from '@/utils/request'

/**
 * 系统管理 API 模块
 */

/**
 * 获取系统信息
 */
export function getSystemInfo() {
    return request({
        url: '/system/info',
        method: 'get'
    })
}

/**
 * 获取系统构建版本标识
 */
export function getSystemBuildTime() {
    return request({
        url: '/system/build-time',
        method: 'get'
    })
}
