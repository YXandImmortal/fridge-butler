import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import UserIndexView from "@/views/user/UserIndexView.vue";
import SuperAdminIndex from "@/views/super-admin/SuperAdminIndex.vue";
import UserCenterView from "@/views/user/UserCenterView.vue";
import FridgeListView from "@/views/fridge/FridgeListView.vue";
import FridgeDetailView from "@/views/fridge/FridgeDetailView.vue";
import FridgeCreateView from "@/views/fridge/FridgeCreateView.vue";

const LoginView = () => import('@/views/LoginView.vue')
const RegisterView = () => import('@/views/RegisterView.vue')

const SUPER_ADMIN_PERMISSION = 1
const USER_PERMISSION = 2

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/user',
      children: [
        {
          path: 'index',
          name: 'user-index',
          component: UserIndexView,
          meta: {
            requiresAuth: true,
            roles: [USER_PERMISSION]
          }
        },
        {
          path: 'center',
          name: 'user-center',
          component: UserCenterView,
          meta: {
            requiresAuth: true,
            roles: [USER_PERMISSION]
          }
        }
      ]
    },
    {
      path: '/fridge',
      children: [
        {
          path: 'list',
          name: 'fridge-list',
          component: FridgeListView,
          meta: {
            requiresAuth: true,
            roles: [USER_PERMISSION]
          }
        },
        {
          path: 'create',
          name: 'fridge-create',
          component: FridgeCreateView,
          meta: {
            requiresAuth: true,
            roles: [USER_PERMISSION]
          }
        },
        {
          path: 'detail/:id?',
          name: 'fridge-detail',
          component: FridgeDetailView,
          meta: {
            requiresAuth: true,
            roles: [USER_PERMISSION]
          }
        }
      ]
    },
    {
      path: '/super-admin/index',
      name: 'super-admin-index',
      component: SuperAdminIndex,
      meta: {
        requiresAuth: true,
        roles: [SUPER_ADMIN_PERMISSION] // 超级管理员 roleId=1
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
// 注意：用户状态已在 main.js 中通过 initUser() 初始化，此处直接读取即可
router.beforeEach((to, from) => {
  const userStore = useUserStore()

  // 检查路由是否需要认证
  if (to.meta?.requiresAuth) {
    // 用户未登录，重定向到登录页
    if (!userStore.isLoggedIn) {
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
  if (userStore.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    const userRoleId = userStore.roleId
    if (userRoleId === 1) {
      return { name: 'super-admin-index' }
    } else if (userRoleId === 2) {
      return { name: 'user-index' }
    }
  }
})

export default router
