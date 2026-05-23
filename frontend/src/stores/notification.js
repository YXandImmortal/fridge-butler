import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
    getUnreadCount,
    getNotificationSummary,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    getNotificationList
} from '@/api/notification'

const POLL_INTERVAL = 30000 // 轮询间隔 30 秒

export const useNotificationStore = defineStore('notification', () => {
    // ========== 状态 ==========
    const unreadCount = ref(0)
    const summary = ref({
        totalUnread: 0,
        expiredCount: 0,
        expiringCriticalCount: 0,
        expiringWarningCount: 0,
        expiringNoticeCount: 0,
        capacityWarningCount: 0,
        systemCount: 0
    })
    const notificationList = ref([])
    const totalCount = ref(0)
    const currentType = ref('') // 当前筛选类型
    const loading = ref(false)
    const pollTimer = ref(null)

    // ========== 计算属性 ==========
    const hasUnread = computed(() => unreadCount.value > 0)

    const summaryItems = computed(() => [
        { key: '', label: '全部', count: summary.value.totalUnread, icon: 'icon-notification', color: 'var(--primary-color)' },
        { key: 'EXPIRED', label: '已过期', count: summary.value.expiredCount, icon: 'icon-calendar-alert', color: 'var(--danger-color)' },
        { key: 'EXPIRING_CRITICAL', label: '1天内过期', count: summary.value.expiringCriticalCount, icon: 'icon-clock', color: 'var(--danger-color)' },
        { key: 'EXPIRING_WARNING', label: '3天内过期', count: summary.value.expiringWarningCount, icon: 'icon-clock', color: 'var(--warn-color)' },
        { key: 'EXPIRING_NOTICE', label: '7天内过期', count: summary.value.expiringNoticeCount, icon: 'icon-clock', color: 'var(--text-tertiary)' },
        { key: 'CAPACITY_WARNING', label: '容量预警', count: summary.value.capacityWarningCount, icon: 'icon-chart', color: 'var(--warn-color)' },
        { key: 'SYSTEM', label: '系统通知', count: summary.value.systemCount, icon: 'icon-info-box', color: 'var(--text-tertiary)' }
    ])

    // ========== 方法 ==========

    /**
     * 获取未读消息总数
     */
    const fetchUnreadCount = async () => {
        try {
            const res = await getUnreadCount()
            if (res.code === 200 && typeof res.data === 'number') {
                unreadCount.value = res.data
            }
        } catch (error) {
            console.error('获取未读消息数失败:', error)
        }
    }

    /**
     * 获取消息摘要统计
     */
    const fetchSummary = async () => {
        try {
            const res = await getNotificationSummary()
            if (res.code === 200 && res.data) {
                summary.value = {
                    totalUnread: res.data.totalUnread || 0,
                    expiredCount: res.data.expiredCount || 0,
                    expiringCriticalCount: res.data.expiringCriticalCount || 0,
                    expiringWarningCount: res.data.expiringWarningCount || 0,
                    expiringNoticeCount: res.data.expiringNoticeCount || 0,
                    capacityWarningCount: res.data.capacityWarningCount || 0,
                    systemCount: res.data.systemCount || 0
                }
            }
        } catch (error) {
            console.error('获取消息摘要失败:', error)
        }
    }

    /**
     * 获取消息列表（不分页，一次性拉取全部）
     */
    const fetchList = async (type = '') => {
        loading.value = true
        currentType.value = type
        try {
            const res = await getNotificationList({ type, page: 1, size: 9999 })
            if (res.code === 200 && res.data) {
                // 兼容两种返回格式：直接数组 或 { list, total, pages }
                if (Array.isArray(res.data)) {
                    notificationList.value = res.data
                    totalCount.value = res.data.length
                } else {
                    notificationList.value = res.data.list || []
                    totalCount.value = res.data.total || 0
                }
            }
        } catch (error) {
            console.error('获取消息列表失败:', error)
        } finally {
            loading.value = false
        }
    }

    /**
     * 标记单条已读
     */
    const readOne = async (id) => {
        try {
            const res = await markAsRead(id)
            if (res.code === 200) {
                // 本地更新状态
                const item = notificationList.value.find(n => n.id === id)
                if (item) {
                    item.status = 'READ'
                    item.readTime = new Date().toISOString()
                }
                // 刷新统计
                await fetchUnreadCount()
                await fetchSummary()
                return true
            }
        } catch (error) {
            console.error('标记已读失败:', error)
        }
        return false
    }

    /**
     * 一键全部已读
     */
    const readAll = async () => {
        try {
            const res = await markAllAsRead()
            if (res.code === 200) {
                // 本地批量更新
                notificationList.value.forEach(item => {
                    item.status = 'READ'
                    item.readTime = new Date().toISOString()
                })
                unreadCount.value = 0
                await fetchSummary()
                return true
            }
        } catch (error) {
            console.error('一键已读失败:', error)
        }
        return false
    }

    /**
     * 删除单条消息
     */
    const removeOne = async (id) => {
        try {
            const res = await deleteNotification(id)
            if (res.code === 200) {
                const idx = notificationList.value.findIndex(n => n.id === id)
                if (idx !== -1) {
                    const wasUnread = notificationList.value[idx].status === 'UNREAD'
                    notificationList.value.splice(idx, 1)
                    if (wasUnread) {
                        await fetchUnreadCount()
                        await fetchSummary()
                    }
                }
                return true
            }
        } catch (error) {
            console.error('删除消息失败:', error)
        }
        return false
    }

    /**
     * 根据 actionType 获取跳转路由
     */
    const getActionRoute = (notification) => {
        const { actionType, actionPayload } = notification
        if (!actionType || actionType === 'NONE' || !actionPayload) {
            return null
        }
        switch (actionType) {
            case 'VIEW_ITEM':
                if (actionPayload.itemId && actionPayload.fridgeId) {
                    return `/fridge/items/${actionPayload.fridgeId}?itemId=${actionPayload.itemId}`
                }
                return null
            case 'VIEW_FRIDGE':
                if (actionPayload.fridgeId) {
                    return `/fridge/detail/${actionPayload.fridgeId}`
                }
                return null
            default:
                return null
        }
    }

    /**
     * 启动轮询
     */
    const startPolling = () => {
        stopPolling()
        fetchUnreadCount()
        pollTimer.value = setInterval(() => {
            fetchUnreadCount()
        }, POLL_INTERVAL)
    }

    /**
     * 停止轮询
     */
    const stopPolling = () => {
        if (pollTimer.value) {
            clearInterval(pollTimer.value)
            pollTimer.value = null
        }
    }

    /**
     * 初始化（获取摘要+启动轮询）
     */
    const init = async () => {
        await fetchSummary()
        await fetchUnreadCount()
        startPolling()
    }

    return {
        unreadCount,
        summary,
        notificationList,
        totalCount,
        currentType,
        loading,
        hasUnread,
        summaryItems,
        fetchUnreadCount,
        fetchSummary,
        fetchList,
        readOne,
        readAll,
        removeOne,
        getActionRoute,
        startPolling,
        stopPolling,
        init
    }
})
