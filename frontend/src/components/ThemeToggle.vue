<template>
  <button
    class="theme-toggle"
    :class="{ 'is-dark': themeStore.theme === 'dark' }"
    :aria-label="themeStore.theme === 'dark' ? '切换到浅色主题' : '切换到深色主题'"
    @click="themeStore.toggleTheme"
  >
    <div class="theme-toggle-track">
      <div class="theme-toggle-thumb">
        <transition name="theme-icon" mode="out-in">
          <svg
            v-if="themeStore.theme === 'dark'"
            key="moon"
            class="theme-icon"
            viewBox="0 0 24 24"
            fill="currentColor"
          >
            <path d="M12 3a9 9 0 1 0 9 9c0-.46-.04-.92-.1-1.36a5.389 5.389 0 0 1-4.4 2.26 5.403 5.403 0 0 1-3.14-9.8c-.44-.06-.9-.1-1.36-.1z"/>
          </svg>
          <svg
            v-else
            key="sun"
            class="theme-icon"
            viewBox="0 0 24 24"
            fill="currentColor"
          >
            <path d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1zM5.99 4.58a.996.996 0 00-1.41 0 .996.996 0 000 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41L5.99 4.58zm12.37 12.37a.996.996 0 00-1.41 0 .996.996 0 000 1.41l1.06 1.06c.39.39 1.03.39 1.41 0 .39-.39.39-1.03 0-1.41l-1.06-1.06zm1.06-10.96a.996.996 0 000-1.41.996.996 0 00-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06zM7.05 18.36a.996.996 0 000-1.41.996.996 0 00-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06z"/>
          </svg>
        </transition>
      </div>
      <!-- 装饰星星（仅在深色模式显示） -->
      <span v-if="themeStore.theme === 'dark'" class="star star-1">✦</span>
      <span v-if="themeStore.theme === 'dark'" class="star star-2">✦</span>
    </div>
  </button>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
</script>

<style scoped>
.theme-toggle {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  outline: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
  border-radius: 20px;
}

.theme-toggle-track {
  position: relative;
  width: 52px;
  height: 28px;
  background: var(--input-bg);
  border-radius: 14px;
  border: 1px solid var(--border-color);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.theme-toggle:hover .theme-toggle-track {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-20);
}

.theme-toggle.is-dark .theme-toggle-track {
  background: linear-gradient(135deg, #1e3a5f 0%, #0f172a 100%);
  border-color: rgba(100, 181, 246, 0.3);
}

.theme-toggle-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  background: var(--card-bg);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-toggle.is-dark .theme-toggle-thumb {
  transform: translateX(24px);
  background: var(--text-primary);
}

.theme-icon {
  width: 14px;
  height: 14px;
  transition: color 0.3s ease;
}

.theme-toggle:not(.is-dark) .theme-icon {
  color: #f59e0b;
}

.theme-toggle.is-dark .theme-icon {
  color: #94a3b8;
}

/* 装饰星星 */
.star {
  position: absolute;
  color: var(--primary-color);
  font-size: 6px;
  opacity: 0;
  animation: twinkle 2s ease-in-out infinite;
}

.star-1 {
  top: 5px;
  right: 8px;
  animation-delay: 0s;
}

.star-2 {
  top: 14px;
  right: 4px;
  animation-delay: 0.8s;
}

.theme-toggle.is-dark .star {
  opacity: 0.6;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

/* 图标过渡动画 */
.theme-icon-enter-active,
.theme-icon-leave-active {
  transition: all 0.25s ease;
}

.theme-icon-enter-from {
  opacity: 0;
  transform: rotate(-90deg) scale(0.5);
}

.theme-icon-leave-to {
  opacity: 0;
  transform: rotate(90deg) scale(0.5);
}
</style>
