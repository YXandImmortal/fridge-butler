import axios from 'axios'
import {useUserStore} from '@/stores/user'
import showMessage from '@/utils/message'
import {replaceToLogin, toForbidden, toServerError, toServiceUnavailable, toActivation} from '@/utils/navigate'

// 创建axios实例
const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL, // 后端接口基础地址（在.env文件配置）
    timeout: 120000 // 请求超时时间
})

// 请求拦截器：添加token到请求头
service.interceptors.request.use(
    (config) => {
        const userStore = useUserStore()
        // 如果有token，添加到请求头（后端鉴权用）
        if (userStore.token) {
            // 检查token是否过期
            if (userStore.expireTime && Date.now() > userStore.expireTime) {
                showMessage.warning('登录已过期，请重新登录')
                userStore.logout()
                // 跳转到登录页
                replaceToLogin()
                return Promise.reject(new Error('Token expired'))
            }
            config.headers['Authorization'] = `Bearer ${userStore.token}`
        }
        return config
    },
    (error) => {
        Promise.reject(error)
    }
)

// 响应拦截器：统一处理后端返回结果
service.interceptors.response.use(
    (response) => {
        const data = response.data
        // 用户未激活（业务码 460，HTTP 状态码 200）
        if (data.code === 460) {
            const userStore = useUserStore()
            userStore.setActivationStatus(false)
            showMessage.warning(data.message || '账号未激活，请先输入激活密钥')
            toActivation()
            return Promise.reject(new Error(data.message || '账号未激活'))
        }
        return data
    },
    (error) => {
        if (error.response) {
            const status = error.response.status
            // 处理401未授权错误
            if (status === 401) {
                const userStore = useUserStore()
                userStore.logout()
                showMessage.error('登录已过期，请重新登录')
                replaceToLogin()
            } else if (status === 403) {
                // 权限不足，跳转403页面
                toForbidden()
            } else if (status === 500) {
                // 服务器内部错误，跳转500页面
                toServerError()
            } else if (status === 503) {
                // 服务不可用，跳转503页面（预留）
                toServiceUnavailable()
            } else {
                showMessage.error(error.message || '服务器错误')
            }
        } else {
            // 后端无响应、网络错误或请求超时
            toServerError()
        }
        return Promise.reject(error)
    }
)

export default service