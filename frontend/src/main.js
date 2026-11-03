import {createApp} from 'vue'
import {createPinia} from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import App from './App.vue'
import router from './router'
import {useUserStore} from "@/stores/user.js";
import {useThemeStore} from "@/stores/theme.js";
import {setRouter} from '@/utils/navigate'

import './styles/index.scss'
import '@/assets/iconfont/iconfont.css'
// Symbol 彩色图标（需先从 iconfont 项目下载 iconfont.js 放到 src/assets/iconfont/ 目录）
import '@/assets/iconfont/iconfont.js'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, {locale: zhCn})

// 将 router 实例注册到导航工具，供 axios 拦截器使用
setRouter(router)

const userStore = useUserStore()
userStore.initUser()

// 初始化主题 Store，内部完成主题加载并在挂载前应用
const themeStore = useThemeStore()

app.mount('#app')