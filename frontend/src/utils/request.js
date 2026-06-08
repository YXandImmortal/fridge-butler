import axios from 'axios'
import {useUserStore} from '@/stores/user'
import showMessage from '@/utils/message'
import {replaceToLogin, toForbidden, toServerError, toServiceUnavailable, toActivation, toAccountDisabled} from '@/utils/navigate'

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
        // 兼容旧版：账号未激活仍可能返回 HTTP 200
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
            const data = error.response.data
            // 优先读取后端返回的业务错误消息
            const backendMessage = data?.message
            const isLoginRequest = error.config?.url?.includes('/auth/login')

            // 处理401未授权错误
            if (status === 401) {
                if (isLoginRequest) {
                    showMessage.error(backendMessage || '用户名或密码错误')
                } else {
                    const userStore = useUserStore()
                    userStore.logout()
                    showMessage.error(backendMessage || '登录已过期，请重新登录')
                    replaceToLogin()
                }
            } else if (status === 403) {
                // 业务码 461 = 账号被禁用；登录接口的 403 也视为账号被禁用
                if (isLoginRequest || data?.code === 461) {
                    const userStore = useUserStore()
                    userStore.logout()
                    showMessage.error(backendMessage || '账号已被禁用')
                    toAccountDisabled()
                } else {
                    // 权限不足，跳转403页面
                    showMessage.error(backendMessage || '权限不足')
                    toForbidden()
                }
            } else if (status === 404) {
                showMessage.error(backendMessage || '请求的资源不存在')
            } else if (status === 500) {
                // 服务器内部错误，跳转500页面
                showMessage.error(backendMessage || '服务器内部错误')
                toServerError()
            } else if (status === 503) {
                const isAiRequest = error.config?.url?.includes('/ai/chat')
                if (isAiRequest) {
                    // AI 聊天接口的 503 只弹提示不跳转页面，避免中断聊天体验
                    showMessage.error(backendMessage || 'AI 服务繁忙，请稍后重试')
                } else {
                    // 服务不可用，跳转503页面（预留）
                    showMessage.error(backendMessage || '服务暂时不可用，请稍后重试')
                    toServiceUnavailable()
                }
            } else if (status === 400) {
                if (data?.code === 460) {
                    // 账号未激活
                    const userStore = useUserStore()
                    userStore.setActivationStatus(false)
                    showMessage.warning(backendMessage || '账号未激活，请先输入激活密钥')
                    toActivation()
                } else {
                    // 参数校验失败等通用业务错误
                    showMessage.error(backendMessage || error.message || '请求参数错误')
                }
            } else {
                showMessage.error(backendMessage || error.message || '服务器错误')
            }
        } else {
            // 后端无响应、网络错误或请求超时
            showMessage.error('网络错误，请检查网络连接')
            toServerError()
        }
        return Promise.reject(error)
    }
)

export default service