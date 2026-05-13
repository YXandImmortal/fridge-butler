import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import App from './App.vue'
import router from './router'
import { useUserStore } from "@/stores/user.js";
import { useThemeStore } from "@/stores/theme.js";
import { setRouter } from '@/utils/navigate'

import './styles/index.scss'
import '@/assets/iconfont/iconfont.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 将 router 实例注册到导航工具，供 axios 拦截器使用
setRouter(router)

const userStore = useUserStore()
userStore.initUser()

// 初始化主题（在挂载前应用，避免闪烁）
const themeStore = useThemeStore()
themeStore.loadTheme()

app.mount('#app')