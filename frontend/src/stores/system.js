import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useSystemStore = defineStore('system', () => {
    const systemName = ref('')
    const systemVersion = ref('')
    const userIndexFeathers = ref([])
    const isLoading = ref(false)

    // 从localStorage加载数据
    const loadFromLocalStorage = () => {
        const storedData = localStorage.getItem('systemInfo')
        if (storedData) {
            try {
                const parsedData = JSON.parse(storedData)
                systemName.value = parsedData.systemName || ''
                systemVersion.value = parsedData.systemVersion || ''
                userIndexFeathers.value = parsedData.userIndexFeathers || []
            } catch (error) {
                console.error('Error loading system info from localStorage:', error)
                // 清除损坏的数据
                localStorage.removeItem('systemInfo')
            }
        }
    }

    // 保存数据到localStorage
    const saveToLocalStorage = () => {
        const data = {
            systemName: systemName.value,
            systemVersion: systemVersion.value,
            userIndexFeathers: userIndexFeathers.value
        }
        localStorage.setItem('systemInfo', JSON.stringify(data))
    }

    // 从后端获取数据
    const fetchSystemInfoFromBackend = async () => {
        try {
            const res = await request({
                method: 'get',
                url: '/system/info'
            })
            return res.data || {}
        } catch (error) {
            console.error('获取系统信息失败：', error)
            return null
        }
    }

    // 获取系统信息（核心方法）
    // 检查版本号是否变更，如果变更从后端获取最新数据，否则使用localStorage中的数据
    const getSystemInfo = async () => {
        if (isLoading.value) {
            // 防止重复请求
            return { systemName: systemName.value, systemVersion: systemVersion.value, userIndexFeathers: userIndexFeathers.value }
        }

        isLoading.value = true
        
        try {
            // 先从后端获取版本信息
            const backendInfo = await fetchSystemInfoFromBackend()
            
            if (!backendInfo) {
                // 如果后端请求失败，使用本地数据
                return { systemName: systemName.value, systemVersion: systemVersion.value, userIndexFeathers: userIndexFeathers.value }
            }

            // 检查版本号是否变更
            if (systemVersion.value !== backendInfo.systemVersion) {
                // 版本号变更，更新数据
                systemName.value = backendInfo.systemName || ''
                systemVersion.value = backendInfo.systemVersion || ''
                userIndexFeathers.value = backendInfo.userIndexFeathers || []
                
                // 保存到localStorage
                saveToLocalStorage()
            }

            // 返回最新数据
            return { systemName: systemName.value, systemVersion: systemVersion.value, userIndexFeathers: userIndexFeathers.value }
        } finally {
            isLoading.value = false
        }
    }

    // 初始化时加载数据
    loadFromLocalStorage()

    return { 
        systemName, 
        systemVersion, 
        userIndexFeathers,
        isLoading,
        getSystemInfo,
        loadFromLocalStorage
    }
})