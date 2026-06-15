import {showExpNotify} from './expNotify'
import {showBadgeNotify} from './badgeNotify'
import {checkLevelUp, showLevelUpNotify} from './levelUpNotify'
import {useGamificationStore} from '@/stores/gamification'

// 成就总览防抖刷新定时器
let refreshTimer = null
// 延迟时间：最后一次奖励后 1.5 秒刷新，避免连续操作时频繁请求
const REFRESH_DEBOUNCE_MS = 1500

/**
 * 防抖刷新成就总览
 * 用于非升级场景下，EXP / 徽章变化后延迟更新 Header 等处的展示
 */
const debouncedRefreshOverview = () => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }
  refreshTimer = setTimeout(async () => {
    refreshTimer = null
    try {
      const store = useGamificationStore()
      await store.fetchOverview(true)
    } catch (err) {
      console.error('刷新成就总览失败:', err)
    }
  }, REFRESH_DEBOUNCE_MS)
}

/**
 * 游戏化反馈统一通知
 * 根据后端响应中的 expGained / badgesUnlocked 展示 EXP 和徽章解锁提示
 *
 * @param {Object} res - 后端响应对象（已校验 code === 200）
 * @param {string} [description] - EXP 行为描述，如"添加食材"
 * @param {Object} [options]
 * @param {boolean} [options.showZeroExp=false] - 是否即使 expGained 为 0 也展示提示（一般保持 false）
 */
export function notifyGamificationResult(res, description = '', options = {}) {
  if (!res || res.code !== 200 || !res.data) {
    return
  }

  const reward = {
    expGained: res.data?.expGained ?? 0,
    dailyExpToday: res.data?.dailyExpToday ?? 0,
    dailyExpLimit: res.data?.dailyExpLimit ?? 0,
    leveledUp: res.data?.leveledUp ?? false,
    currentLevel: res.data?.currentLevel ?? null,
    level: res.data?.level ?? null,
    badgesUnlocked: res.data?.badgesUnlocked || []
  }

  notifyGamificationReward(reward, description, options)
}

/**
 * 根据 reward 对象展示 EXP、徽章解锁和等级提升提示
 * 用于 SSE reward 事件或同步兜底奖励数据
 *
 * @param {Object} reward - 奖励对象
 * @param {string} [description] - EXP 行为描述
 * @param {Object} [options]
 * @param {boolean} [options.showZeroExp=false]
 */
export async function notifyGamificationReward(reward, description = '', options = {}) {
  if (!reward) return

  const store = useGamificationStore()
  const {showZeroExp = false} = options
  const expGained = reward.expGained ?? 0
  const badgesUnlocked = reward.badgesUnlocked || []

  if ((expGained > 0 || showZeroExp) && description) {
    showExpNotify.gain(expGained, description)
  }

  if (badgesUnlocked.length > 0) {
    showBadgeNotify.batch(badgesUnlocked)
    // 强制刷新徽章墙缓存，确保个人中心页面徽章及时更新
    store.fetchBadges(true)
  }

  // 如果后端明确告知升级，优先使用 reward.level 直接展示弹窗
  if (reward.leveledUp) {
    try {
      if (refreshTimer) {
        clearTimeout(refreshTimer)
        refreshTimer = null
      }

      if (reward.level) {
        // 直接使用后端返回的最新等级信息，避免等待 overview 请求
        const currentLevel = reward.level.currentLevel
        const lastLevel = parseInt(sessionStorage.getItem('gamification_last_level') || '0', 10) || 0
        if (currentLevel > lastLevel) {
          showLevelUpNotify({
            level: currentLevel,
            title: reward.level.title,
            totalExp: reward.level.totalExp,
            icons: reward.level.icons,
            nextLevel: reward.level.nextLevel
          })
          sessionStorage.setItem('gamification_last_level', String(currentLevel))
        }
        // 后台刷新 overview，保证 Header 等处的数据最新
        store.fetchOverview(true).catch(err => console.error('刷新成就总览失败:', err))
      } else {
        // 兼容旧逻辑：没有 level 字段时仍然刷新 overview
        await store.fetchOverview(true)
        checkLevelUp()
      }
    } catch (err) {
      console.error('刷新成就总览失败:', err)
    }
    return
  }

  // 非升级场景下，若 EXP 或徽章有变化，延迟刷新 overview（防抖）
  if (expGained > 0 || badgesUnlocked.length > 0) {
    debouncedRefreshOverview()
  }
}

/**
 * 消费待展示奖励队列，按顺序展示 EXP、徽章、升级弹窗。
 * 消费过程中新产生的奖励会继续被消费，直到队列为空。
 */
let consumeLock = false

export async function consumePendingRewards() {
  const store = useGamificationStore()
  if (consumeLock) return
  consumeLock = true
  let hasBadgeReward = false

  try {
    while (store.pendingRewards.length > 0) {
      const rewards = store.consumePendingRewards()
      for (let i = 0; i < rewards.length; i++) {
        const reward = rewards[i]
        switch (reward.type) {
          case 'exp':
            showExpNotify.gain(reward.exp, reward.description || '成就奖励')
            break
          case 'badge':
            showBadgeNotify({
              code: reward.badge?.code || '',
              name: reward.badge?.name || '',
              iconClass: reward.badge?.iconClass || '',
              expReward: reward.badge?.expReward || 0
            })
            hasBadgeReward = true
            break
          case 'levelUp': {
            const currentLevel = reward.levelInfo?.currentLevel ?? 1
            const lastLevel = parseInt(sessionStorage.getItem('gamification_last_level') || '0', 10) || 0
            // 如果已经记录过更高或相同等级，跳过避免重复展示
            if (currentLevel > lastLevel) {
              showLevelUpNotify({
                level: currentLevel,
                title: reward.levelInfo?.title || '',
                totalExp: reward.levelInfo?.totalExp || 0,
                icons: reward.levelInfo?.icons || {},
                nextLevel: reward.levelInfo?.nextLevel || null
              })
              sessionStorage.setItem('gamification_last_level', String(currentLevel))
            }
            break
          }
          default:
            console.warn('未知奖励类型:', reward.type)
        }
        if (i < rewards.length - 1) {
          await new Promise((resolve) => setTimeout(resolve, 300))
        }
      }
    }
    // 如果消费过程中展示了徽章奖励，强制刷新徽章墙缓存
    if (hasBadgeReward) {
      await store.fetchBadges(true)
    }
  } finally {
    consumeLock = false
  }
}

export default notifyGamificationResult
