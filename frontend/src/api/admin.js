import request from '@/utils/request'

// ========== 数据看板 ==========
export const getDashboardStats = () => request.get('/admin/dashboard/stats')
export const getDashboardTrend = (days) => request.get('/admin/dashboard/trend', { params: { days } })

// ========== 用户管理 ==========
export const getUserList = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, null, { params: { status } })
export const resetUserPassword = (id) => request.post(`/admin/users/${id}/reset-password`)
export const getUserDetail = (id) => request.get(`/admin/users/${id}/detail`)

// ========== 操作日志 ==========
export const getLogList = (params) => request.get('/admin/logs', { params })

// ========== 系统配置 ==========
export const getSystemConfig = () => request.get('/admin/system/config')
export const updateSystemConfig = (data) => request.put('/admin/system/config', data)

// ========== 重要通知广播 ==========
// 已迁移至 @/api/notification.js，此处保留 re-export 以保持兼容
export { broadcastImportantNotice } from '@/api/notification.js'

// ========== 密钥管理 ==========
export const getActivationKeyList = (params) => request.get('/admin/activation-keys', { params })
export const generateActivationKeys = (data) => request.post('/admin/activation-keys', data)
export const revokeActivationKey = (id) => request.put(`/admin/activation-keys/${id}/revoke`)
export const issueActivationKey = (id) => request.put(`/admin/activation-keys/${id}/issue`)
export const destroyActivationKey = (id) => request.delete(`/admin/activation-keys/${id}`)
