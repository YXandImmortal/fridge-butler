import request from '@/utils/request'

/**
 * 成就与游戏化系统 API 模块
 */

/**
 * 进入数据中心，触发 EXP 与徽章结算
 */
export function enterDataCenter() {
    return request({
        url: '/gamification/data-center/enter',
        method: 'post'
    })
}

/**
 * 获取成就总览
 * 包含等级、EXP、冰鲜连续天数、保鲜评分、热力图缩略、设置等聚合数据
 */
export function getGamificationOverview() {
    return request({
        url: '/gamification/overview',
        method: 'get'
    })
}

/**
 * 获取 EXP 日志
 * @param {Object} params - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.size=20] - 每页条数
 */
export function getExpLog(params = {}) {
    return request({
        url: '/gamification/exp-log',
        method: 'get',
        params
    })
}

/**
 * 获取徽章墙
 * 包含已获得与未获得徽章列表
 */
export function getBadges() {
    return request({
        url: '/gamification/badges',
        method: 'get'
    })
}

/**
 * 获取实时保鲜评分
 */
export function getFreshnessScore() {
    return request({
        url: '/gamification/freshness-score',
        method: 'get'
    })
}

/**
 * 获取保鲜评分热力图
 * @param {number} [days=90] - 统计天数，支持 90/180/365
 */
export function getHeatmap(days = 90) {
    return request({
        url: '/gamification/heatmap',
        method: 'get',
        params: {days}
    })
}

/**
 * 获取月度报告
 * @param {string} yearMonth - 年月，格式 yyyy-MM
 */
export function getMonthlyReport(yearMonth) {
    return request({
        url: '/gamification/monthly-report',
        method: 'get',
        params: {yearMonth}
    })
}

/**
 * 查看月度报告后结算奖励
 * 仅首次查看发放 EXP，后端幂等
 * @param {string} yearMonth - 年月，格式 yyyy-MM
 */
export function viewMonthlyReport(yearMonth) {
    return request({
        url: '/gamification/monthly-report/view',
        method: 'post',
        data: {yearMonth}
    })
}

/**
 * 获取成就设置
 */
export function getAchievementSettings() {
    return request({
        url: '/gamification/settings',
        method: 'get'
    })
}

/**
 * 更新成就设置
 * @param {Object} data - 设置参数
 * @param {boolean} [data.panelHidden] - 是否隐藏成就面板
 * @param {boolean} [data.autoStreakProtect] - 是否自动保护冰鲜连续天数
 * @param {boolean} [data.streakProtectNotify] - 是否发送保护通知
 */
export function updateAchievementSettings(data) {
    return request({
        url: '/gamification/settings/update',
        method: 'post',
        data
    })
}
