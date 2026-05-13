import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

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

    // 切换主题
    const toggleTheme = () => {
        theme.value = theme.value === 'light' ? 'dark' : 'light'
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
