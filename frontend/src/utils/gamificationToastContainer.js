/**
 * 游戏化 Toast 共享容器
 *
 * ExpToast、BadgeToast 等轻量通知统一挂载到同一个固定容器内，
 * 按调用顺序自上而下排列，避免多个独立容器重叠堆叠。
 */

let container = null

export function getGamificationToastContainer() {
  if (!container) {
    container = document.createElement('div')
    container.className = 'gamification-toast-container'
    container.style.cssText = `
      position: fixed;
      top: calc(var(--header-height) + var(--space-4));
      right: var(--space-5);
      z-index: 9998;
      display: flex;
      flex-direction: column;
      gap: var(--space-3);
      pointer-events: none;
    `
    document.body.appendChild(container)
  }
  return container
}

/**
 * 清理共享容器（一般用于退出登录等需要重置全局状态的场景）
 */
export function destroyGamificationToastContainer() {
  if (container) {
    container.remove()
    container = null
  }
}
