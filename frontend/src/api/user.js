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
 * 初始化密码（首次设置密码，无需原密码和验证码）
 * @param {Object} data - 密码数据
 * @param {string} data.newPassword - 新密码
 * @param {string} data.confirmNewPassword - 确认密码
 */
export function initPassword(data) {
    return request({
        url: '/user/init-password',
        method: 'post',
        data
    })
}

/**
 * 发送绑定/修改邮箱验证码（需登录）
 * @param {string} email - 邮箱地址
 */
export function sendBindEmailCaptcha(email) {
    return request({
        url: '/user/email/captcha',
        method: 'post',
        data: { email }
    })
}

/**
 * 绑定/修改邮箱（需登录）
 * @param {Object} data
 * @param {string} data.email - 邮箱地址
 * @param {string} data.captcha - 验证码
 */
export function bindEmail(data) {
    return request({
        url: '/user/email',
        method: 'post',
        data
    })
}

/**
 * 检查当前登录用户是否已绑定合法邮箱
 * @returns {Promise<{code: number, data: boolean, message?: string}>}
 */
export function checkEmailBound() {
    return request({
        url: '/user/email/bound',
        method: 'get'
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
