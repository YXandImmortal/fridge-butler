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

/**
 * 获取公开系统配置（公告、系统描述等）
 */
export function getPublicConfig() {
    return request({
        url: '/system/public-config',
        method: 'get'
    })
}
