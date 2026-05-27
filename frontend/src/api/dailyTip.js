import request from '@/utils/request'

/**
 * 每日小贴士 API 模块
 */

/**
 * 获取今日小贴士
 */
export function getTodayTip() {
    return request({
        url: '/daily-tip/today',
        method: 'get'
    })
}

/**
 * 获取指定日期的小贴士
 * @param {string} date - 日期，格式 yyyy-MM-dd
 */
export function getTipByDate(date) {
    return request({
        url: '/daily-tip/by-date',
        method: 'get',
        params: {date}
    })
}
