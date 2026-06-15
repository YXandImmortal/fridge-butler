import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import {
    getGamificationOverview,
    getExpLog,
    getBadges,
    getFreshnessScore,
    getHeatmap,
    getMonthlyReport,
    getAchievementSettings,
    updateAchievementSettings
} from '@/api/gamification'

/**
 * EXP 行为类型中文映射
 */
export const ACTION_TYPE_MAP = {
    LOGIN: '每日登录',
    NO_EXPIRE: '今日无过期',
    CONSUME_EXPIRING: '消耗临期食材',
    ADD_ITEM: '添加食材',
    ORGANIZE: '整理冰箱',
    VIEW_DATA_CENTER: '查看数据中心',
    AI_CHAT: '与 AI 对话',
    BADGE: '获得徽章',
    STREAK_BONUS: '冰鲜连续加成',
    SCORE_BREAKTHROUGH: '评分突破',
    MONTHLY_REPORT: '查看月度报告',
    BIND_EMAIL: '绑定邮箱',
    GUIDE: '完成新手指引',
    SHARE: '分享成就卡片'
}

/**
 * 保鲜评分等级映射
 */
export const SCORE_GRADE_MAP = {
    S: {label: 'S', min: 90, max: 100, color: '#22c55e'},
    A: {label: 'A', min: 80, max: 89, color: '#3b82f6'},
    B: {label: 'B', min: 70, max: 79, color: '#eab308'},
    C: {label: 'C', min: 60, max: 69, color: '#f97316'},
    D: {label: 'D', min: 0, max: 59, color: '#ef4444'}
}

/**
 * 根据分数获取保鲜等级
 * @param {number} score - 保鲜评分（-1 表示无有效数据，返回 null）
 * @returns {{label: string, color: string}|null}
 */
export function getScoreGrade(score) {
    if (score === null || score === undefined || score < 0) {
        return null
    }
    for (const grade of Object.values(SCORE_GRADE_MAP)) {
        if (score >= grade.min && score <= grade.max) {
            return grade
        }
    }
    return SCORE_GRADE_MAP.D
}

export const useGamificationStore = defineStore('gamification', () => {
    // ==================== 状态 ====================
    const overview = ref(null)
    const pendingRewards = ref([])
    const badges = ref([])
    const expLog = ref({
        records: [],
        total: 0,
        page: 1,
        size: 20,
        pages: 0
    })
    const freshnessScore = ref(null)
    const heatmap = ref({
        90: null,
        180: null,
        365: null
    })
    const monthlyReports = ref({})
    const settings = ref(null)

    const loading = ref({
        overview: false,
        badges: false,
        expLog: false,
        freshnessScore: false,
        heatmap: false,
        monthlyReport: false,
        settings: false,
        updateSettings: false
    })

    // 缓存时间戳（毫秒）
    const cacheTime = ref({
        overview: 0,
        badges: 0,
        freshnessScore: 0,
        settings: 0
    })

    // 默认缓存有效期 5 分钟
    const CACHE_TTL = 5 * 60 * 1000

    // ==================== 计算属性 ====================
    const isOverviewLoading = computed(() => loading.value.overview)
    const isBadgesLoading = computed(() => loading.value.badges)
    const isExpLogLoading = computed(() => loading.value.expLog)
    const isFreshnessScoreLoading = computed(() => loading.value.freshnessScore)
    const isHeatmapLoading = computed(() => loading.value.heatmap)
    const isSettingsLoading = computed(() => loading.value.settings)

    const unlockedBadges = computed(() =>
        (badges.value || []).filter((badge) => badge.unlocked)
    )

    const lockedBadges = computed(() =>
        (badges.value || []).filter((badge) => !badge.unlocked)
    )

    const recentBadges = computed(() => {
        return [...unlockedBadges.value]
            .sort((a, b) => new Date(b.unlockedAt || 0) - new Date(a.unlockedAt || 0))
            .slice(0, 6)
    })

    const isPanelHidden = computed(() => !!settings.value?.panelHidden)
    const autoStreakProtect = computed(() => !!settings.value?.autoStreakProtect)
    const streakProtectNotify = computed(() => !!settings.value?.streakProtectNotify)

    // ==================== 私有辅助函数 ====================
    const isCacheValid = (key) => {
        const timestamp = cacheTime.value[key]
        if (!timestamp) return false
        return Date.now() - timestamp < CACHE_TTL
    }

    const updateCacheTime = (key) => {
        cacheTime.value[key] = Date.now()
    }

    // ==================== 异步方法 ====================

    /**
     * 将奖励推入待展示队列
     * @param {Object} reward - 奖励对象
     */
    const pushPendingReward = (reward) => {
        pendingRewards.value.push({
            id: `${reward.type}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
            ...reward,
            createdAt: Date.now()
        })
    }

    /**
     * 消费并清空所有待展示奖励
     * @returns {Array} 待展示奖励列表
     */
    const consumePendingRewards = () => {
        if (pendingRewards.value.length === 0) return []
        const rewards = [...pendingRewards.value]
        pendingRewards.value = []
        return rewards
    }

    /**
     * 获取成就总览
     * @param {boolean} [force=false] - 是否强制刷新，忽略缓存
     */
    const fetchOverview = async (force = false) => {
        if (loading.value.overview) return overview.value
        if (!force && isCacheValid('overview') && overview.value) {
            return overview.value
        }

        loading.value.overview = true
        try {
            const res = await getGamificationOverview()
            if (res.code === 200 && res.data) {
                overview.value = res.data

                // 将一次性奖励转移到 pendingRewards，避免被缓存反复展示
                const expGained = overview.value.expGained ?? 0
                const badgesUnlocked = overview.value.badgesUnlocked || []
                const leveledUp = overview.value.leveledUp === true

                if (expGained > 0) {
                    pushPendingReward({
                        type: 'exp',
                        exp: expGained,
                        description: '评分突破',
                        source: 'overview'
                    })
                }
                badgesUnlocked.forEach((badge) => {
                    pushPendingReward({
                        type: 'badge',
                        badge,
                        source: 'overview'
                    })
                })
                if (badgesUnlocked.length > 0) {
                    // 有徽章解锁时强制刷新徽章墙，避免个人中心页面展示旧数据
                    fetchBadges(true)
                }
                if (leveledUp && overview.value.level) {
                    pushPendingReward({
                        type: 'levelUp',
                        levelInfo: overview.value.level,
                        source: 'overview'
                    })
                }

                // 清空 overview 中的一次性奖励字段
                overview.value.expGained = 0
                overview.value.badgesUnlocked = []
                overview.value.leveledUp = false

                updateCacheTime('overview')
            }
            return overview.value
        } catch (error) {
            console.error('获取成就总览失败:', error)
            return overview.value
        } finally {
            loading.value.overview = false
        }
    }

    /**
     * 获取徽章墙
     * @param {boolean} [force=false] - 是否强制刷新
     */
    const fetchBadges = async (force = false) => {
        if (loading.value.badges) return badges.value
        if (!force && isCacheValid('badges') && badges.value.length > 0) {
            return badges.value
        }

        loading.value.badges = true
        try {
            const res = await getBadges()
            if (res.code === 200 && Array.isArray(res.data)) {
                badges.value = res.data
                updateCacheTime('badges')
            }
            return badges.value
        } catch (error) {
            console.error('获取徽章墙失败:', error)
            return badges.value
        } finally {
            loading.value.badges = false
        }
    }

    /**
     * 获取 EXP 日志
     * @param {Object} params - 查询参数
     * @param {number} [params.page=1] - 页码
     * @param {number} [params.size=20] - 每页条数
     */
    const fetchExpLog = async (params = {}) => {
        const page = params.page || 1
        const size = params.size || 20

        if (loading.value.expLog) return expLog.value

        loading.value.expLog = true
        try {
            const res = await getExpLog({page, size})
            if (res.code === 200 && res.data) {
                const records = res.data.records || res.data.list || []
                const total = res.data.total || 0
                expLog.value = {
                    records,
                    total,
                    page: res.data.page || page,
                    size: res.data.size || size,
                    pages: res.data.pages || Math.ceil(total / size) || 0
                }
            }
            return expLog.value
        } catch (error) {
            console.error('获取 EXP 日志失败:', error)
            return expLog.value
        } finally {
            loading.value.expLog = false
        }
    }

    /**
     * 获取实时保鲜评分
     * @param {boolean} [force=false] - 是否强制刷新
     */
    const fetchFreshnessScore = async (force = false) => {
        if (loading.value.freshnessScore) return freshnessScore.value
        if (!force && isCacheValid('freshnessScore') && freshnessScore.value) {
            return freshnessScore.value
        }

        loading.value.freshnessScore = true
        try {
            const res = await getFreshnessScore()
            if (res.code === 200 && res.data) {
                freshnessScore.value = res.data
                updateCacheTime('freshnessScore')
            }
            return freshnessScore.value
        } catch (error) {
            console.error('获取保鲜评分失败:', error)
            return freshnessScore.value
        } finally {
            loading.value.freshnessScore = false
        }
    }

    /**
     * 获取热力图数据
     * @param {number} [days=90] - 统计天数
     */
    const fetchHeatmap = async (days = 90) => {
        const validDays = [90, 180, 365].includes(days) ? days : 90

        if (loading.value.heatmap) return heatmap.value[validDays]
        if (heatmap.value[validDays]) {
            return heatmap.value[validDays]
        }

        loading.value.heatmap = true
        try {
            const res = await getHeatmap(validDays)
            if (res.code === 200 && res.data) {
                heatmap.value[validDays] = res.data
            }
            return heatmap.value[validDays]
        } catch (error) {
            console.error(`获取热力图(${validDays}天)失败:`, error)
            return heatmap.value[validDays]
        } finally {
            loading.value.heatmap = false
        }
    }

    /**
     * 获取月度报告
     * @param {string} yearMonth - 年月，格式 yyyy-MM
     */
    const fetchMonthlyReport = async (yearMonth) => {
        if (!yearMonth) return null
        if (loading.value.monthlyReport) return monthlyReports.value[yearMonth]
        if (monthlyReports.value[yearMonth]) {
            return monthlyReports.value[yearMonth]
        }

        loading.value.monthlyReport = true
        try {
            const res = await getMonthlyReport(yearMonth)
            if (res.code === 200 && res.data) {
                monthlyReports.value[yearMonth] = res.data
            }
            return monthlyReports.value[yearMonth]
        } catch (error) {
            console.error(`获取月度报告(${yearMonth})失败:`, error)
            return monthlyReports.value[yearMonth]
        } finally {
            loading.value.monthlyReport = false
        }
    }

    /**
     * 获取成就设置
     * @param {boolean} [force=false] - 是否强制刷新
     */
    const fetchSettings = async (force = false) => {
        if (loading.value.settings) return settings.value
        if (!force && isCacheValid('settings') && settings.value) {
            return settings.value
        }

        loading.value.settings = true
        try {
            const res = await getAchievementSettings()
            if (res.code === 200 && res.data) {
                settings.value = res.data
                updateCacheTime('settings')
            }
            return settings.value
        } catch (error) {
            console.error('获取成就设置失败:', error)
            return settings.value
        } finally {
            loading.value.settings = false
        }
    }

    /**
     * 更新成就设置
     * @param {Object} data - 设置参数
     */
    const updateSettings = async (data) => {
        if (loading.value.updateSettings) return null

        loading.value.updateSettings = true
        try {
            const res = await updateAchievementSettings(data)
            if (res.code === 200) {
                // 刷新本地设置缓存
                await fetchSettings(true)
            }
            return res
        } catch (error) {
            console.error('更新成就设置失败:', error)
            throw error
        } finally {
            loading.value.updateSettings = false
        }
    }

    /**
     * 重置所有成就数据（如退出登录时调用）
     */
    const reset = () => {
        overview.value = null
        badges.value = []
        expLog.value = {
            records: [],
            total: 0,
            page: 1,
            size: 20,
            pages: 0
        }
        freshnessScore.value = null
        heatmap.value = {90: null, 180: null, 365: null}
        monthlyReports.value = {}
        settings.value = null
        pendingRewards.value = []
        cacheTime.value = {
            overview: 0,
            badges: 0,
            freshnessScore: 0,
            settings: 0
        }
    }

    return {
        // 状态
        overview,
        pendingRewards,
        badges,
        expLog,
        freshnessScore,
        heatmap,
        monthlyReports,
        settings,
        loading,
        cacheTime,

        // 计算属性
        isOverviewLoading,
        isBadgesLoading,
        isExpLogLoading,
        isFreshnessScoreLoading,
        isHeatmapLoading,
        isSettingsLoading,
        unlockedBadges,
        lockedBadges,
        recentBadges,
        isPanelHidden,
        autoStreakProtect,
        streakProtectNotify,

        // 方法
        fetchOverview,
        pushPendingReward,
        consumePendingRewards,
        fetchBadges,
        fetchExpLog,
        fetchFreshnessScore,
        fetchHeatmap,
        fetchMonthlyReport,
        fetchSettings,
        updateSettings,
        reset
    }
})
