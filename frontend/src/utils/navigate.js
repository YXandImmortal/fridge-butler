// 导航工具模块
// 避免 axios 拦截器与 router 之间产生循环依赖

let routerInstance = null

export function setRouter(router) {
    routerInstance = router
}

/**
 * 跳转到登录页（保留历史记录）
 */
export function toLogin() {
    if (routerInstance) {
        routerInstance.push({name: 'login'})
    } else {
        window.location.href = '/login'
    }
}

/**
 * 替换到登录页（不保留历史记录，适合 401 等强制退出场景）
 */
export function replaceToLogin() {
    if (routerInstance) {
        routerInstance.replace({name: 'login'})
    } else {
        window.location.href = '/login'
    }
}

/**
 * 跳转到 403 页面
 */
export function toForbidden() {
    if (routerInstance) {
        routerInstance.push({name: 'forbidden'})
    } else {
        window.location.href = '/403'
    }
}

/**
 * 跳转到 500 页面
 */
export function toServerError() {
    if (routerInstance) {
        routerInstance.push({name: 'server-error'})
    } else {
        window.location.href = '/500'
    }
}

/**
 * 跳转到 503 页面（预留）
 */
export function toServiceUnavailable() {
    if (routerInstance) {
        routerInstance.push({name: 'service-unavailable'})
    } else {
        window.location.href = '/503'
    }
}

/**
 * 替换到激活页（不保留历史记录）
 */
export function toActivation() {
    if (routerInstance) {
        routerInstance.replace({name: 'activation'})
    } else {
        window.location.href = '/activation'
    }
}
