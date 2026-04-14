import axios from 'axios'
import {ElMessage} from 'element-plus'
import {useUserStore} from '@/stores/user'

// 创建axios实例
const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL, // 后端接口基础地址（在.env文件配置）
    timeout: 5000 // 请求超时时间
})

// 请求拦截器：添加token到请求头
service.interceptors.request.use(
    (config) => {
        const userStore = useUserStore()
        // 如果有token，添加到请求头（后端鉴权用）
        if (userStore.token) {
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
        ElMessage.error(error.message || '服务器错误')
        return Promise.reject(error)
    }
)

export default service