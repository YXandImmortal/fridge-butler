import {defineStore} from 'pinia'
import {ref, watch} from 'vue'

const THEME_STORAGE_KEY = 'app-theme'

export const useThemeStore = defineStore('theme', () => {
    const theme = ref('light')
    const reducedMotionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
    let activeTransition = null
    let activeAnimation = null

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

    const clearTransitionState = () => {
        document.documentElement.classList.remove('theme-switching')
        activeTransition = null
        activeAnimation = null
    }

    const cleanupTransition = (transition) => {
        if (transition && activeTransition !== transition) return
        clearTransitionState()
    }

    const cancelActiveTransition = () => {
        if (activeTransition) {
            try {
                activeTransition.skip()
            } catch {
                // 已结束或尚未开始的过渡无需处理
            }
        }
        activeAnimation?.cancel()
        clearTransitionState()
    }

    const getTransitionOrigin = (event) => {
        const rect = event?.currentTarget?.getBoundingClientRect?.()
        if (rect && (rect.width > 0 || rect.height > 0)) {
            return {
                x: rect.left + rect.width / 2,
                y: rect.top + rect.height / 2
            }
        }
        return {
            x: window.innerWidth / 2,
            y: window.innerHeight / 2
        }
    }

    // 切换主题（带扩散动画）
    const toggleTheme = (event) => {
        const nextTheme = theme.value === 'dark' ? 'light' : 'dark'

        // 减少动态效果偏好及不支持 View Transitions API 时直接切换
        if (reducedMotionQuery.matches || !document.startViewTransition) {
            cancelActiveTransition()
            theme.value = nextTheme
            return
        }

        cancelActiveTransition()

        const {x, y} = getTransitionOrigin(event)
        const endRadius = Math.hypot(
            Math.max(x, window.innerWidth - x),
            Math.max(y, window.innerHeight - y)
        )

        const html = document.documentElement
        html.classList.add('theme-switching')

        let transition
        try {
            transition = document.startViewTransition(() => {
                theme.value = nextTheme
            })
        } catch {
            theme.value = nextTheme
            clearTransitionState()
            return
        }
        activeTransition = transition

        transition.ready.then(() => {
            if (activeTransition !== transition) return
            activeAnimation = html.animate(
                {
                    clipPath: [
                        `circle(0px at ${x}px ${y}px)`,
                        `circle(${endRadius}px at ${x}px ${y}px)`
                    ]
                },
                {
                    duration: 400,
                    easing: 'cubic-bezier(0.4, 0, 0.2, 1)',
                    pseudoElement: '::view-transition-new(root)'
                }
            )
            return activeAnimation.finished
        }).catch(() => {
            // 动画取消或页面隐藏导致的拒绝无需打断主题切换
        })

        transition.finished.then(
            () => cleanupTransition(transition),
            () => cleanupTransition(transition)
        )
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
