import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
    // 状态变量（对应后端LoginResponse的字段）
    const token = ref('')
    const username = ref('')
    const roleName = ref('')
    const roleId = ref('')
    const userId = ref('')
    const rememberMe = ref(false)
    const expireTime = ref(0)

    // 初始化：从localStorage加载状态（页面刷新后保留登录状态）
    const initUser = () => {
        const userInfo = localStorage.getItem('userInfo')
        const sessionInfo = sessionStorage.getItem('userInfo')

        let info = null;
        if (userInfo) {
            info = JSON.parse(userInfo)
        } else if (sessionInfo) {
            info = JSON.parse(sessionInfo)
        }

        if (info) {
            token.value = info.token
            username.value = info.username
            roleName.value = info.roleName
            roleId.value = info.roleId
            userId.value = info.userId
            rememberMe.value = info.rememberMe || false
            expireTime.value = info.expireTime || 0

            // 检查token是否过期
            if (expireTime.value && Date.now() > expireTime.value) {
                // token已过期，清除存储
                logout()
                return false
            }
            return true
        }
        return false
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
            const {
                token: resToken,
                username: resName,
                roleName: resRole,
                roleId: resRoleId,
                userId: resId,
                rememberMe: resRememberMe,
                expireTime: resExpireTime
            } = res.data

            // 更新状态
            token.value = resToken
            username.value = resName
            roleName.value = resRole
            roleId.value = resRoleId
            userId.value = resId
            rememberMe.value = resRememberMe || false
            expireTime.value = resExpireTime || 0

            // 构建用户信息对象
            const userInfo = {
                token: resToken,
                username: resName,
                roleName: resRole,
                roleId: resRoleId,
                userId: resId,
                rememberMe: resRememberMe || false,
                expireTime: resExpireTime || 0
            }

            // 根据rememberMe决定存储方式
            if (resRememberMe) {
                // 勾选了"记住我"，使用localStorage长期存储
                localStorage.setItem('userInfo', JSON.stringify(userInfo))
                // 清除sessionStorage中的旧数据
                sessionStorage.removeItem('userInfo')
            } else {
                // 未勾选"记住我"，使用sessionStorage会话存储
                sessionStorage.setItem('userInfo', JSON.stringify(userInfo))
                // 清除localStorage中的旧数据
                localStorage.removeItem('userInfo')
            }
        }

        return res
    }

    // 退出登录：清空状态和本地存储
    const logout = () => {
        token.value = ''
        username.value = ''
        roleName.value = ''
        roleId.value = ''
        userId.value = ''
        rememberMe.value = false
        expireTime.value = 0
        localStorage.removeItem('userInfo')
        sessionStorage.removeItem('userInfo')
    }

    return { token, username, roleName, roleId, userId, rememberMe, expireTime, initUser, login, logout }
})