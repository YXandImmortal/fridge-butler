import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
    // 状态变量（对应后端LoginResponse的字段）
    const token = ref('')
    const username = ref('')
    const roleName = ref('')
    const userId = ref('')

    // 初始化：从localStorage加载状态（页面刷新后保留登录状态）
    const initUser = () => {
        const userInfo = localStorage.getItem('userInfo')
        if (userInfo) {
            const info = JSON.parse(userInfo)
            token.value = info.token
            username.value = info.username
            roleName.value = info.roleName
            userId.value = info.userId
        }
    }

    // 登录方法：调用后端/auth/login接口
    const login = async (loginForm) => {
        const res = await request({
            url: '/auth/login',
            method: 'post',
            data: {
                account: loginForm.account,
                password: loginForm.password,
                rememberMe: loginForm.rememberMe
            }
        })

        // 只有当code为200时才更新用户状态
        if (res.code === 200 && res.data) {
            const { token: resToken, username: resName, roleName: resRole, userId: resId } = res.data
            // 更新状态
            token.value = resToken
            username.value = resName
            roleName.value = resRole
            userId.value = resId
            // 持久化到localStorage
            localStorage.setItem('userInfo', JSON.stringify({
                token: resToken,
                username: resName,
                roleName: resRole,
                userId: resId
            }))
        }

        return res
    }

    // 退出登录：清空状态和本地存储
    const logout = () => {
        token.value = ''
        username.value = ''
        roleName.value = ''
        userId.value = ''
        localStorage.removeItem('userInfo')
    }

    return { token, username, roleName, userId, initUser, login, logout }
})
