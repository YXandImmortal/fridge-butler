import {createVNode, render} from 'vue'
import LevelUpNotify from '@/components/gamification/LevelUpNotify.vue'
import {useGamificationStore} from '@/stores/gamification'

/**
 * 等级升级通知工具
 * 用于在用户等级提升时展示全局庆祝弹窗
 *
 * 使用方式：
 *   import {checkLevelUp, showLevelUpNotify} from '@/utils/levelUpNotify'
 *   checkLevelUp() // 自动对比前后等级并展示弹窗
 */

let container = null
let instances = []
let levelUpNotifyId = 0

const LEVEL_STORAGE_KEY = 'gamification_last_level'

const getContainer = () => {
  if (!container) {
    container = document.createElement('div')
    container.className = 'level-up-notify-container'
    container.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      pointer-events: none;
      z-index: 9999;
    `
    document.body.appendChild(container)
  }
  return container
}

const removeLevelUpNotify = (id) => {
  const index = instances.findIndex((item) => item.id === id)
  if (index > -1) {
    const {el} = instances[index]
    render(null, el)
    el.remove()
    instances.splice(index, 1)
  }
}

/**
 * 显示等级升级弹窗
 * @param {Object} options
 * @param {number} options.level - 当前等级
 * @param {string} [options.title] - 当前称号
 * @param {number} [options.totalExp] - 累计 EXP
 * @param {Object} [options.icons] - 当前等级图标集合
 * @param {Object} [options.nextLevel] - 下一级信息 {level, title, requiredExp, icons}
 * @param {number} [options.duration=4000] - 显示时长（毫秒）
 */
export function showLevelUpNotify(options = {}) {
  const {
    level = 1,
    title = '',
    totalExp = 0,
    icons = {},
    nextLevel = null,
    duration = 4000
  } = options

  const id = `level-up-notify-${++levelUpNotifyId}`
  const instance = createVNode(LevelUpNotify, {
    id,
    level,
    title,
    totalExp,
    icons,
    nextLevel,
    duration,
    onClose: () => removeLevelUpNotify(id)
  })

  const el = document.createElement('div')
  el.id = id
  el.style.pointerEvents = 'auto'
  getContainer().appendChild(el)

  render(instance, el)
  instances.push({id, el})
}

/**
 * 便捷方法：根据 store 中的 overview 直接展示升级弹窗
 */
showLevelUpNotify.fromOverview = () => {
  const store = useGamificationStore()
  const levelInfo = store.overview?.level
  if (!levelInfo) return

  showLevelUpNotify({
    level: levelInfo.currentLevel,
    title: levelInfo.title,
    totalExp: levelInfo.totalExp,
    icons: levelInfo.icons,
    nextLevel: levelInfo.nextLevel
  })
}

/**
 * 获取上次记录的最高等级
 * @returns {number}
 */
const getLastLevel = () => {
  const value = sessionStorage.getItem(LEVEL_STORAGE_KEY)
  if (!value) return 0
  const parsed = parseInt(value, 10)
  return Number.isNaN(parsed) ? 0 : parsed
}

/**
 * 记录当前等级，避免重复提示
 * @param {number} level
 */
const setLastLevel = (level) => {
  sessionStorage.setItem(LEVEL_STORAGE_KEY, String(level))
}

/**
 * 检测等级是否提升，若提升则展示升级弹窗
 *
 * @param {boolean} [forceShow=false] - 是否忽略缓存强制展示（调试用）
 * @returns {boolean} 是否触发了升级弹窗
 */
export function checkLevelUp(forceShow = false) {
  const store = useGamificationStore()
  const levelInfo = store.overview?.level

  if (!levelInfo || !levelInfo.currentLevel) {
    return false
  }

  const currentLevel = levelInfo.currentLevel
  const lastLevel = getLastLevel()

  // 首次进入无缓存时，记录当前等级但不展示
  if (lastLevel === 0 && !forceShow) {
    setLastLevel(currentLevel)
    return false
  }

  if (currentLevel > lastLevel || forceShow) {
    showLevelUpNotify({
      level: currentLevel,
      title: levelInfo.title,
      totalExp: levelInfo.totalExp,
      icons: levelInfo.icons,
      nextLevel: levelInfo.nextLevel
    })
    setLastLevel(currentLevel)
    return true
  }

  return false
}

/**
 * 重置等级缓存（退出登录时调用）
 */
export function resetLevelUpNotify() {
  sessionStorage.removeItem(LEVEL_STORAGE_KEY)
  // 清理所有未关闭的弹窗
  instances.forEach(({el}) => {
    render(null, el)
    el.remove()
  })
  instances = []
}

export default showLevelUpNotify
