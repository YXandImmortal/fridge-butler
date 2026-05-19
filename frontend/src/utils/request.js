import axios from 'axios'
import { useUserStore } from '@/stores/user'
import showMessage from '@/utils/message'
import { replaceToLogin } from '@/utils/navigate'

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
        // 直接返回后端数据，由调用方处理业务逻辑
        return response.data
    },
    (error) => {
        // 处理401未授权错误
        if (error.response && error.response.status === 401) {
            const userStore = useUserStore()
            userStore.logout()
            showMessage.error('登录已过期，请重新登录')
            replaceToLogin()
        } else {
            showMessage.error(error.message || '服务器错误')
        }
        return Promise.reject(error)
    }
)

export default service