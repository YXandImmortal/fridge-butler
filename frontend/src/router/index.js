import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { MainLayout } from '@/layouts'

const UserIndexView = () => import('@/views/user/UserIndexView.vue')
const SuperAdminIndex = () => import('@/views/super-admin/SuperAdminIndexView.vue')
const UserCenterView = () => import('@/views/user/UserCenterView.vue')
const FridgeListView = () => import('@/views/fridge/FridgeListView.vue')
const FridgeDetailView = () => import('@/views/fridge/FridgeDetailView.vue')
const ItemManageView = () => import('@/views/fridge/ItemManageView.vue')
const ItemCategoryListView = () => import('@/views/item/ItemCategoryListView.vue')
const DataCenterView = () => import('@/views/data-center/DataCenterView.vue')
const AboutView = () => import('@/views/AboutView.vue')

const LoginView = () => import('@/views/LoginView.vue')
const RegisterView = () => import('@/views/RegisterView.vue')

const SUPER_ADMIN_PERMISSION = 1
const USER_PERMISSION = 2

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: { name: 'login' }
    },
    // 用户端路由（使用主布局）
    {
      path: '/user',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'index',
          name: 'user-index',
          component: UserIndexView,
          meta: {
            roles: [USER_PERMISSION]
          }
        },
        {
          path: 'center',
          name: 'user-center',
          component: UserCenterView,
          meta: {
            roles: [USER_PERMISSION]
          }
        },
        {
          path: 'about',
          name: 'user-about',
          component: AboutView,
          meta: {
            roles: [USER_PERMISSION]
          }
        }
      ]
    },
    // 冰箱管理路由（使用主布局）
    {
      path: '/fridge',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'list',
          name: 'fridge-list',
          component: FridgeListView,
          meta: {
            roles: [USER_PERMISSION]
          }
        },
        {
          path: 'detail/:id?',
          name: 'fridge-detail',
          component: FridgeDetailView,
          meta: {
            roles: [USER_PERMISSION]
          }
        },
        {
          path: 'items/:id?',
          name: 'fridge-items',
          component: ItemManageView,
          meta: {
            roles: [USER_PERMISSION]
          }
        }
      ]
    },
    // 数据中心路由（使用主布局）
    {
      path: '/data-center',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'index',
          name: 'data-center',
          component: DataCenterView,
          meta: {
            roles: [USER_PERMISSION]
          }
        }
      ]
    },
    // 物品分类管理路由（使用主布局）
    {
      path: '/item-category',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'list',
          name: 'item-category-list',
          component: ItemCategoryListView,
          meta: {
            roles: [USER_PERMISSION]
          }
        },
      ]
    },
    // 物品单位管理路由（使用主布局）
    {
      path: '/item-unit-type',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'list',
          name: 'item-unit-type-list',
          component: () => import('@/views/item/ItemUnitTypeListView.vue'),
          meta: {
            roles: [USER_PERMISSION]
          }
        },
      ]
    },
    // 超级管理员路由（使用主布局）
    {
      path: '/super-admin',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'index',
          name: 'super-admin-index',
          component: SuperAdminIndex,
          meta: {
            roles: [SUPER_ADMIN_PERMISSION]
          }
        }
      ]
    },
    // 公开路由（不使用主布局）
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
    // 错误页面路由
    {
      path: '/403',
      name: 'forbidden',
      component: () => import('@/views/error/ForbiddenView.vue')
    },
    {
      path: '/500',
      name: 'server-error',
      component: () => import('@/views/error/ServerErrorView.vue')
    },
    {
      path: '/503',
      name: 'service-unavailable',
      component: () => import('@/views/error/ServiceUnavailableView.vue')
    },
    // 404路由（放在最后）
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/error/NotFoundView.vue')
    }
  ]
})

// 路由守卫：验证登录状态和权限
// 注意：用户状态已在 main.js 中通过 initUser() 初始化，此处直接读取即可
router.beforeEach((to, from) => {
  const userStore = useUserStore()

  // 检查路由是否需要认证（包括父路由）
  const requiresAuth = to.matched.some(record => record.meta?.requiresAuth)

  if (requiresAuth) {
    // 用户未登录，重定向到登录页
    if (!userStore.isLoggedIn) {
      return {
        name: 'login',
        query: { redirect: to.fullPath } // 保存目标路由，登录后跳转
      }
    }

    // 检查用户角色权限
    const requiredRoles = to.meta?.roles
    if (requiredRoles && requiredRoles.length > 0) {
      const userRoleId = userStore.roleId
      if (!requiredRoles.includes(userRoleId)) {
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
