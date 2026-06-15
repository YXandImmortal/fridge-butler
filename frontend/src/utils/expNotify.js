import {createVNode, render} from 'vue'
import ExpToast from '@/components/gamification/ExpToast.vue'
import {getGamificationToastContainer} from './gamificationToastContainer'

/**
 * EXP 获得通知工具
 * 用于在用户执行游戏化相关操作后展示轻量 +N EXP 提示
 */

let instances = []
let expToastId = 0

const removeExpToast = (id) => {
  const index = instances.findIndex(item => item.id === id)
  if (index > -1) {
    const {el} = instances[index]
    render(null, el)
    el.remove()
    instances.splice(index, 1)
  }
}

/**
 * 显示 EXP 获得提示
 * @param {Object} options
 * @param {number} options.exp - 获得的 EXP 数值
 * @param {string} [options.description] - 行为描述，如"添加食材"
 * @param {number} [options.duration=2500] - 显示时长（毫秒）
 */
export function showExpNotify(options = {}) {
  const {
    exp = 0,
    description = '',
    duration = 2500
  } = options

  // 保守模式：只有后端明确返回 expGained > 0 时才展示，避免每日上限/已领取场景误导用户
  if (exp <= 0) {
    return
  }

  const id = `exp-toast-${++expToastId}`
  const instance = createVNode(ExpToast, {
    id,
    exp,
    description,
    duration,
    onClose: () => removeExpToast(id)
  })

  const el = document.createElement('div')
  el.id = id
  el.style.pointerEvents = 'auto'
  getGamificationToastContainer().appendChild(el)

  render(instance, el)
  instances.push({id, el})
}

/**
 * 便捷方法：展示 EXP 获得
 * @param {number} exp - 获得的 EXP 数值
 * @param {string} [description] - 行为描述
 */
showExpNotify.gain = (exp, description) => {
  showExpNotify({exp, description})
}

export default showExpNotify
