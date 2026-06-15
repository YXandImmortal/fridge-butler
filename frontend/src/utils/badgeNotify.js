import {createVNode, render} from 'vue'
import BadgeToast from '@/components/gamification/BadgeToast.vue'
import {getGamificationToastContainer} from './gamificationToastContainer'

/**
 * 徽章解锁通知工具
 * 用于在用户解锁徽章后展示轻量提示
 */

let instances = []
let badgeToastId = 0

const removeBadgeNotify = (id) => {
  const index = instances.findIndex(item => item.id === id)
  if (index > -1) {
    const {el} = instances[index]
    render(null, el)
    el.remove()
    instances.splice(index, 1)
  }
}

/**
 * 展示徽章解锁提示
 * @param {Object} options
 * @param {string} options.code - 徽章编码
 * @param {string} options.name - 徽章名称
 * @param {string} [options.iconClass] - iconfont 类名
 * @param {number} [options.expReward] - 徽章附带 EXP 奖励
 * @param {number} [options.duration=3000] - 显示时长（毫秒）
 */
export function showBadgeNotify(options = {}) {
  const {
    code = '',
    name = '',
    iconClass = '',
    expReward = 0,
    duration = 3000
  } = options

  if (!code && !name) {
    return
  }

  const id = `badge-toast-${++badgeToastId}`
  const instance = createVNode(BadgeToast, {
    id,
    code,
    name,
    iconClass,
    expReward,
    duration,
    onClose: () => removeBadgeNotify(id)
  })

  const el = document.createElement('div')
  el.id = id
  el.style.pointerEvents = 'auto'
  getGamificationToastContainer().appendChild(el)

  render(instance, el)
  instances.push({id, el})
}

/**
 * 便捷方法：展示徽章解锁
 * @param {string} code - 徽章编码
 * @param {string} name - 徽章名称
 * @param {number} [expReward] - 徽章附带 EXP 奖励
 * @param {string} [iconClass] - iconfont 类名
 */
showBadgeNotify.unlock = (code, name, expReward, iconClass) => {
  showBadgeNotify({code, name, expReward, iconClass})
}

/**
 * 批量展示徽章解锁提示
 * @param {Array<{code:string,name:string,iconClass?:string,expReward?:number}>} badges
 */
showBadgeNotify.batch = (badges = []) => {
  badges.forEach((badge, index) => {
    setTimeout(() => {
      showBadgeNotify({
        code: badge.code,
        name: badge.name,
        iconClass: badge.iconClass,
        expReward: badge.expReward
      })
    }, index * 200)
  })
}

export default showBadgeNotify
