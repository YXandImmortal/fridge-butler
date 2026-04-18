import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { useUserStore } from "@/stores/user.js";

import './assets/theme.css'
import '@/assets/iconfont/iconfont.css'

const app = createApp(App)
const pinia = createPinia()

// 全局注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

const userStore = useUserStore()
userStore.initUser()

app.mount('#app')