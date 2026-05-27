import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import {
    login as loginApi,
    getUserInfo as getUserInfoApi,
    updateUserAvatar as updateUserAvatarApi,
    updateUserInfo as updateUserInfoApi,
    changePassword as changePasswordApi,
    completeGuide as completeGuideApi
} from '@/api/user'

export const useUserStore = defineStore('user', () => {
    // 状态变量（对应后端LoginResponse的字段）
    const token = ref('')
    const username = ref('')
    const mobile = ref('')
    const createTime = ref('')
    const roleName = ref('')
    const roleId = ref('')
    const userId = ref('')
    const rememberMe = ref(false)
    const avatar = ref('')
    const expireTime = ref(0)
    const guideCompleted = ref(false)

    // 计算属性：是否已登录
    const isLoggedIn = computed(() => !!token.value)

    // 辅助函数：同步指定字段到本地存储
    const syncFieldToStorage = (field, value) => {
        let storageInfo = null
        let storageType = null

        const localInfo = localStorage.getItem('userInfo')
        if (localInfo) {
            storageInfo = JSON.parse(localInfo)
            storageType = 'local'
        } else {
            const sessionInfo = sessionStorage.getItem('userInfo')
            if (sessionInfo) {
                storageInfo = JSON.parse(sessionInfo)
                storageType = 'session'
            }
        }

        if (storageInfo) {
            storageInfo[field] = value
            if (storageType === 'local') {
                localStorage.setItem('userInfo', JSON.stringify(storageInfo))
            } else if (storageType === 'session') {
                sessionStorage.setItem('userInfo', JSON.stringify(storageInfo))
            }
        }
    }

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
            mobile.value = info.mobile
            createTime.value = info.createTime
            roleName.value = info.roleName
            roleId.value = info.roleId
            userId.value = info.userId
            rememberMe.value = info.rememberMe || false
            avatar.value = info.avatar
            expireTime.value = info.expireTime || 0
            guideCompleted.value = info.guideCompleted || false

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
        const res = await loginApi(loginForm)

        // 只有当code为200时才更新用户状态
        if (res.code === 200 && res.data) {
            const {
                token: resToken,
                username: resName,
                mobile: resMobile,
                createTime: resCreateTime,
                roleName: resRole,
                roleId: resRoleId,
                userId: resId,
                rememberMe: resRememberMe,
                avatar: resAvatar,
                expireTime: resExpireTime,
                guideCompleted: resGuideCompleted
            } = res.data

            // 更新状态
            token.value = resToken
            username.value = resName
            mobile.value = resMobile
            createTime.value = resCreateTime
            roleName.value = resRole
            roleId.value = resRoleId
            userId.value = resId
            rememberMe.value = resRememberMe || false
            avatar.value = resAvatar
            expireTime.value = resExpireTime || 0
            guideCompleted.value = !!resGuideCompleted

            // 构建用户信息对象
            const userInfo = {
                token: resToken,
                username: resName,
                mobile: resMobile,
                createTime: resCreateTime,
                roleName: resRole,
                roleId: resRoleId,
                userId: resId,
                rememberMe: resRememberMe || false,
                expireTime: resExpireTime || 0,
                guideCompleted: !!resGuideCompleted
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
        mobile.value = ''
        createTime.value = ''
        roleName.value = ''
        roleId.value = ''
        userId.value = ''
        rememberMe.value = false
        avatar.value = ''
        expireTime.value = 0
        guideCompleted.value = false
        localStorage.removeItem('userInfo')
        sessionStorage.removeItem('userInfo')
    }

    // 获取用户信息
    const getUserInfo = async () => {
        try {
            // 先从本地存储获取用户信息
            const userInfo = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo');
            if (userInfo) {
                const parsedInfo = JSON.parse(userInfo);
                // 检查token是否过期
                if (parsedInfo.expireTime && Date.now() > parsedInfo.expireTime) {
                    // token已过期，清除存储并请求后端
                    logout();
                } else {
                    // 同步本地 guideCompleted 到 store
                    guideCompleted.value = parsedInfo.guideCompleted || false
                    // 返回本地存储的用户信息
                    return parsedInfo;
                }
            }

            // 本地存储没有或token过期，请求后端
            const res = await getUserInfoApi();

            if (res.code === 200 && res.data) {
                // 从后端更新 guideCompleted
                if (typeof res.data.guideCompleted !== 'undefined') {
                    guideCompleted.value = !!res.data.guideCompleted
                    syncFieldToStorage('guideCompleted', guideCompleted.value)
                }
                return res.data;
            }
            return null;
        } catch (error) {
            console.error('获取用户信息失败:', error);
            return null;
        }
    }

    // 更新用户头像
    const updateUserAvatar = async (avatarData) => {
        try {
            const res = await updateUserAvatarApi(avatarData);

            if (res.code === 200) {
                avatar.value = avatarData;

                // 检查并更新本地存储
                let storageInfo = null;
                let storageType = null;

                // 先检查localStorage
                const localInfo = localStorage.getItem('userInfo');
                if (localInfo) {
                    storageInfo = JSON.parse(localInfo);
                    storageType = 'local';
                } else {
                    // 再检查sessionStorage
                    const sessionInfo = sessionStorage.getItem('userInfo');
                    if (sessionInfo) {
                        storageInfo = JSON.parse(sessionInfo);
                        storageType = 'session';
                    }
                }

                // 如果存在存储的用户信息，则更新
                if (storageInfo) {
                    storageInfo.avatar = avatarData;

                    if (storageType === 'local') {
                        localStorage.setItem('userInfo', JSON.stringify(storageInfo));
                    } else if (storageType === 'session') {
                        sessionStorage.setItem('userInfo', JSON.stringify(storageInfo));
                    }
                }
            }
            return res;
        } catch (error) {
            console.error('更新用户头像失败:', error);
            throw error;
        }
    }

    // 更新用户信息
    const updateUserInfo = async (userInfo) => {
        try {
            const res = await updateUserInfoApi({
                username: userInfo.username,
                mobile: userInfo.mobile
            });

            // 后端更新成功后，更新本地存储和状态
            if (res.code === 200) {
                // 更新状态变量
                username.value = userInfo.username;
                mobile.value = userInfo.mobile;

                // 检查并更新本地存储
                let storageInfo = null;
                let storageType = null;

                // 先检查localStorage
                const localInfo = localStorage.getItem('userInfo');
                if (localInfo) {
                    storageInfo = JSON.parse(localInfo);
                    storageType = 'local';
                } else {
                    // 再检查sessionStorage
                    const sessionInfo = sessionStorage.getItem('userInfo');
                    if (sessionInfo) {
                        storageInfo = JSON.parse(sessionInfo);
                        storageType = 'session';
                    }
                }

                // 如果存在存储的用户信息，则更新
                if (storageInfo) {
                    storageInfo.username = userInfo.username;
                    storageInfo.mobile = userInfo.mobile;

                    if (storageType === 'local') {
                        localStorage.setItem('userInfo', JSON.stringify(storageInfo));
                    } else if (storageType === 'session') {
                        sessionStorage.setItem('userInfo', JSON.stringify(storageInfo));
                    }
                }
            }

            return res;
        } catch (error) {
            console.error('更新用户信息失败:', error);
            throw error;
        }
    }

    // 修改密码
    const changePassword = async (passwordData) => {
        try {
            return await changePasswordApi(passwordData);
        } catch (error) {
            console.error('修改密码失败:', error);
            throw error;
        }
    }

    // 标记新手指引完成
    const markGuideCompleted = async () => {
        try {
            const res = await completeGuideApi()
            if (res.code === 200) {
                guideCompleted.value = true
                syncFieldToStorage('guideCompleted', true)
            }
            return res
        } catch (error) {
            console.error('标记引导完成失败:', error)
            throw error
        }
    }

    return {
        token,
        username,
        mobile,
        createTime,
        roleName,
        roleId,
        userId,
        rememberMe,
        avatar,
        expireTime,
        guideCompleted,
        initUser,
        login,
        logout,
        getUserInfo,
        updateUserInfo,
        changePassword,
        updateUserAvatar,
        isLoggedIn,
        markGuideCompleted
    }
})
