import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import UserIndexView from "@/views/user/UserIndexView.vue";
import SuperAdminIndex from "@/views/super-admin/SuperAdminIndex.vue";

const LoginView = () => import('@/views/LoginView.vue')
const RegisterView = () => import('@/views/RegisterView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/user/index',
      name: 'user-index',
      component: UserIndexView,
      meta: {
        requiresAuth: true,
        roles: [2] // 普通用户 roleId=2
      }
    },
    {
      path: '/super-admin/index',
      name: 'super-admin-index',
      component: SuperAdminIndex,
      meta: {
        requiresAuth: true,
        roles: [1] // 超级管理员 roleId=1
      }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    // 404路由
    {
      path: '/:pathMatch(.*)*',
      redirect: '/user/index'
    }
  ]
})

// 路由守卫：验证登录状态和权限
router.beforeEach((to, from) => {
  const userStore = useUserStore()
  // 初始化用户状态（从localStorage加载）
  const isLoggedIn = userStore.initUser()
  
  // 检查路由是否需要认证
  if (to.meta?.requiresAuth) {
    // 用户未登录，重定向到登录页
    if (!isLoggedIn) {
      return {
        name: 'login',
        query: { redirect: to.fullPath } // 保存目标路由，登录后跳转
      }
    }
    
    // 检查用户角色权限
    if (to.meta?.roles && to.meta.roles.length > 0) {
      const userRoleId = userStore.roleId
      if (!to.meta.roles.includes(userRoleId)) {
        // 权限不足，根据用户角色重定向到对应首页
        if (userRoleId === 1) {
          return { name: 'super-admin-index' }
        } else if (userRoleId === 2) {
          return { name: 'user-index' }
        } else {
          // 未知角色，重定向到登录页
          return { name: 'login' }
        }
      }
    }
  }
  
  // 已登录用户访问登录/注册页，重定向到对应首页
  if (isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    const userRoleId = userStore.roleId
    if (userRoleId === 1) {
      return { name: 'super-admin-index' }
    } else if (userRoleId === 2) {
      return { name: 'user-index' }
    }
  }
})

export default router