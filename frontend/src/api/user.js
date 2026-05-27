import request from '@/utils/request'

/**
 * 用户认证与信息管理 API 模块
 */

/**
 * 用户登录
 * @param {Object} data - 登录参数
 * @param {string} data.account - 账号
 * @param {string} data.password - 密码
 * @param {string} data.captcha - 验证码
 * @param {string} data.captchaId - 验证码ID
 * @param {boolean} data.rememberMe - 是否记住我
 */
export function login(data) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

/**
 * 获取当前登录用户信息
 */
export function getUserInfo() {
    return request({
        url: '/user/info',
        method: 'get'
    })
}

/**
 * 更新用户头像
 * @param {string} avatar - 头像地址或数据
 */
export function updateUserAvatar(avatar) {
    return request({
        url: '/user/update-avatar',
        method: 'patch',
        data: {avatar}
    })
}

/**
 * 更新用户基本信息
 * @param {Object} data - 用户信息
 * @param {string} data.username - 用户名
 * @param {string} data.mobile - 手机号
 */
export function updateUserInfo(data) {
    return request({
        url: '/user/update-info',
        method: 'patch',
        data
    })
}

/**
 * 修改密码
 * @param {Object} data - 密码数据
 */
export function changePassword(data) {
    return request({
        url: '/user/change-password',
        method: 'patch',
        data
    })
}

/**
 * 标记新手指引完成
 */
export function completeGuide() {
    return request({
        url: '/user/guide-complete',
        method: 'patch'
    })
}
