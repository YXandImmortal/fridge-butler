import {defineStore} from 'pinia'
import {ref, watch} from 'vue'

const THEME_STORAGE_KEY = 'app-theme'

export const useThemeStore = defineStore('theme', () => {
    const theme = ref('light')

    // 从 localStorage 加载主题
    const loadTheme = () => {
        const stored = localStorage.getItem(THEME_STORAGE_KEY)
        if (stored && (stored === 'light' || stored === 'dark')) {
            theme.value = stored
        } else {
            // 检测系统偏好
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
            theme.value = prefersDark ? 'dark' : 'light'
        }
        applyTheme()
    }

    // 应用主题到 DOM（使用 Element Plus 官方 dark 类方式）
    const applyTheme = () => {
        const html = document.documentElement
        if (theme.value === 'dark') {
            html.classList.add('dark')
        } else {
            html.classList.remove('dark')
        }
    }

    // 切换主题（带扩散动画）
    const toggleTheme = (event) => {
        const isDark = theme.value === 'dark'

        // 降级：浏览器不支持 View Transitions API 时直接切换
        if (!document.startViewTransition) {
            theme.value = isDark ? 'light' : 'dark'
            return
        }

        // 获取点击位置（无事件时取视口中心）
        const x = event?.clientX ?? window.innerWidth / 2
        const y = event?.clientY ?? window.innerHeight / 2

        // 计算到最远角落的扩散半径
        const endRadius = Math.hypot(
            Math.max(x, window.innerWidth - x),
            Math.max(y, window.innerHeight - y)
        )

        // 启动视图过渡
        const transition = document.startViewTransition(() => {
            theme.value = isDark ? 'light' : 'dark'
        })

        // 自定义圆形扩散动画
        transition.ready.then(() => {
            document.documentElement.animate(
                {
                    clipPath: [
                        `circle(0px at ${x}px ${y}px)`,
                        `circle(${endRadius}px at ${x}px ${y}px)`
                    ]
                },
                {
                    duration: 400,
                    easing: 'ease-in-out',
                    pseudoElement: '::view-transition-new(root)'
                }
            )
        })
    }

    // 设置指定主题
    const setTheme = (newTheme) => {
        if (newTheme === 'light' || newTheme === 'dark') {
            theme.value = newTheme
        }
    }

    // 监听主题变化并持久化
    watch(theme, (newTheme) => {
        localStorage.setItem(THEME_STORAGE_KEY, newTheme)
        applyTheme()
    })

    // 初始化
    loadTheme()

    return {
        theme,
        isDark: () => theme.value === 'dark',
        toggleTheme,
        setTheme,
        loadTheme
    }
})
