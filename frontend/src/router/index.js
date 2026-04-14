import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('../views/RegisterView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
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
      redirect: '/login'
    }
  ]
})

// 路由守卫：验证登录状态
router.beforeEach((to, from) => {
  const userStore = useUserStore()
  // 初始化用户状态（从localStorage加载）
  userStore.initUser()

  // 需要登录但未登录 → 返回登录页（带重定向参数）
  if (to.meta.requiresAuth && !userStore.token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath } // 登录后跳回原目标页
    }
  }
})

export default router