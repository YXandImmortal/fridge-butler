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
