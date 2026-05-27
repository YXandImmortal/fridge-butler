<template>
  <aside class="app-sidebar">
    <!-- 导航菜单 -->
    <nav class="sidebar-nav">
      <ul class="nav-menu">
        <!-- 动态菜单 -->
        <li
            v-for="feature in userIndexFeatures"
            :key="feature.id"
            :class="[
            'nav-item',
            {
              active: isMenuActive(feature),
              expanded: isExpanded(feature.id)
            }
          ]"
        >
          <!-- 有子菜单的情况：点击展开/收起 -->
          <div
              v-if="hasChildren(feature)"
              class="nav-link nav-link--parent"
              @click="toggleExpand(feature.id)"
          >
            <div class="nav-icon">
              <i v-if="feature.icon" class="iconfont" :class="feature.icon"/>
              <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
              </svg>
            </div>
            <span class="nav-text">{{ feature.name }}</span>
            <!-- 展开箭头 -->
            <i class="iconfont icon-chevron-down arrow-icon" :class="{ expanded: isExpanded(feature.id) }"/>
          </div>

          <!-- 无子菜单的情况：router-link 跳转 -->
          <router-link
              v-else
              :to="feature.path"
              class="nav-link"
          >
            <div class="nav-icon">
              <i v-if="feature.icon" class="iconfont" :class="feature.icon"/>
              <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
              </svg>
            </div>
            <span class="nav-text">{{ feature.name }}</span>
          </router-link>

          <!-- 子菜单 -->
          <transition name="submenu">
            <ul v-if="hasChildren(feature) && isExpanded(feature.id)" class="sub-menu">
              <li
                  v-for="child in feature.children"
                  :key="child.id"
                  :class="['sub-nav-item', { active: isActive(child.path) }]"
              >
                <router-link :to="child.path" class="sub-nav-link">
                  <span class="sub-nav-dot"></span>
                  <span class="sub-nav-text">{{ child.name }}</span>
                </router-link>
              </li>
            </ul>
          </transition>
        </li>
      </ul>
    </nav>

    <!-- 每日小贴士 -->
    <div class="daily-tip-section">
      <div class="daily-tip-card">
        <!-- 加载中 -->
        <div v-if="tipLoading" class="tip-loading">
          <div class="tip-loading-spinner"></div>
          <span class="tip-loading-text">正在加载今日小贴士...</span>
        </div>

        <!-- 内容区 -->
        <template v-else-if="tip">
          <!-- 头部：emoji + 类型标签 -->
          <div class="tip-header">
            <span class="tip-emoji">{{ tip.emoji }}</span>
            <span
                class="tip-type-badge"
                :style="typeBadgeStyle"
            >
              {{ tip.typeLabel }}
            </span>
          </div>

          <!-- 标题 -->
          <h4 class="tip-title">{{ tip.title }}</h4>

          <!-- 正文 -->
          <p class="tip-content">{{ tip.content }}</p>

          <!-- 谜语答案（交互式） -->
          <div v-if="tip.type === 'RIDDLE' && tip.answer" class="tip-riddle-answer">
            <button
                v-if="!showAnswer"
                class="tip-answer-btn"
                @click="showAnswer = true"
            >
              看答案 👀
            </button>
            <transition name="fade">
              <p v-if="showAnswer" class="tip-answer-text">
                答案：{{ tip.answer }}
              </p>
            </transition>
          </div>

          <!-- 日期 -->
          <div class="tip-footer">
            <span class="tip-date">{{ tip.date }}</span>
          </div>
        </template>

        <!-- 空状态 -->
        <div v-else class="tip-empty">
          <span class="tip-empty-icon">📝</span>
          <span class="tip-empty-text">今日暂无小贴士</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import {onMounted, ref, computed, watch} from 'vue'
import {useSystemStore} from "@/stores/system.js"
import {useRoute} from 'vue-router'
import {getTodayTip} from '@/api/dailyTip'

const systemStore = useSystemStore()
const {userIndexFeatures, getSystemInfo} = systemStore
const route = useRoute()

// 记录展开的菜单 ID
const expandedMenus = ref(new Set())

const hasChildren = (feature) => {
  return feature.children && feature.children.length > 0
}

const isExpanded = (id) => {
  return expandedMenus.value.has(id)
}

const toggleExpand = (id) => {
  if (expandedMenus.value.has(id)) {
    expandedMenus.value.delete(id)
  } else {
    expandedMenus.value.add(id)
  }
}

// 检查当前路径是否与菜单项路径匹配
// 支持子路由高亮：当前路径以菜单路径开头，且菜单路径不是根路径
const isActive = (path) => {
  if (route.path === path) return true
  return path && path !== '/' && route.path.startsWith(path + '/');
}

// 判断父菜单是否处于激活状态（自身路径匹配 或 任一子菜单匹配）
const isMenuActive = (feature) => {
  if (isActive(feature.path)) return true
  if (hasChildren(feature)) {
    return feature.children.some(child => isActive(child.path))
  }
  return false
}

// 自动展开包含当前激活子路由的父菜单
const autoExpandActiveParent = () => {
  userIndexFeatures.forEach(feature => {
    if (hasChildren(feature)) {
      const hasActiveChild = feature.children.some(child => isActive(child.path))
      if (hasActiveChild) {
        expandedMenus.value.add(feature.id)
      }
    }
  })
}

// 路由变化时自动展开对应的父菜单
watch(() => route.path, () => {
  autoExpandActiveParent()
}, {immediate: true})

onMounted(async () => {
  await getSystemInfo()
  await loadTodayTip()
})

// ==================== 每日小贴士 ====================
const tip = ref(null)
const tipLoading = ref(false)
const showAnswer = ref(false)

const TIP_CACHE_KEY = 'fridge_daily_tip_cache'

// 类型对应的颜色配置
const typeColorMap = {
  FACT: {
    color: 'var(--color-primary-400)',
    bg: 'var(--primary-10)'
  },
  TIP: {
    color: 'var(--color-emerald)',
    bg: 'rgba(129, 199, 132, 0.15)'
  },
  JOKE: {
    color: 'var(--color-pink-500)',
    bg: 'rgba(244, 143, 177, 0.15)'
  },
  RIDDLE: {
    color: 'var(--color-purple-500)',
    bg: 'rgba(179, 157, 219, 0.15)'
  }
}

const typeBadgeStyle = computed(() => {
  const config = typeColorMap[tip.value?.type] || typeColorMap.FACT
  return {
    color: config.color,
    backgroundColor: config.bg
  }
})

// 加载今日小贴士（带本地缓存）
const loadTodayTip = async () => {
  tipLoading.value = true
  try {
    // 1. 先尝试读取本地缓存
    const cached = localStorage.getItem(TIP_CACHE_KEY)
    if (cached) {
      try {
        const {date, data} = JSON.parse(cached)
        const today = new Date().toISOString().split('T')[0]
        if (date === today && data) {
          tip.value = data
          tipLoading.value = false
          return
        }
      } catch {
        // 缓存解析失败，继续请求
      }
    }

    // 2. 缓存无效，请求接口
    const res = await getTodayTip()
    if (res.code === 200 && res.data) {
      tip.value = res.data
      // 写入缓存
      const today = new Date().toISOString().split('T')[0]
      localStorage.setItem(TIP_CACHE_KEY, JSON.stringify({
        date: today,
        data: res.data
      }))
    }
  } finally {
    tipLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.app-sidebar {
  width: var(--sidebar-width);
  height: calc(100vh - var(--header-height) - var(--footer-height));
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-sidebar);
  position: fixed;
  top: var(--header-height);
  left: 0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.nav-icon .iconfont {
  font-size: 22px;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  padding: 24px 0;
}

.nav-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-bottom: 4px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 24px;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: 0 12px 12px 0;
  transition: all 0.3s ease;
  position: relative;
  cursor: pointer;
  user-select: none;
}

.nav-link:hover {
  background: var(--primary-10);
  color: var(--primary-color);
  transform: translateX(4px);
}

.nav-item.active > .nav-link,
.nav-item.active > .nav-link--parent {
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 600;
}

.nav-item.active > .nav-link::before,
.nav-item.active > .nav-link--parent::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--primary-color);
  border-radius: 0 4px 4px 0;
}

.nav-icon {
  margin-left: 12px;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.nav-icon svg {
  width: 20px;
  height: 20px;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  flex: 1;
}

/* 展开箭头 */
.arrow-icon {
  width: 18px;
  height: 18px;
  transition: transform 0.3s ease;
  flex-shrink: 0;
  opacity: 0.6;
}

.arrow-icon.expanded {
  transform: rotate(180deg);
}

/* 子菜单 */
.sub-menu {
  list-style: none;
  padding: 4px 0 4px 0;
  margin: 0;
  overflow: hidden;
}

.sub-nav-item {
  margin-bottom: 2px;
}

.sub-nav-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 24px 10px 52px;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: 0 12px 12px 0;
  transition: all 0.3s ease;
  position: relative;
}

.sub-nav-link:hover {
  background: var(--primary-10);
  color: var(--primary-color);
  transform: translateX(4px);
}

.sub-nav-item.active .sub-nav-link {
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 600;
}

.sub-nav-item.active .sub-nav-link::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--primary-color);
  border-radius: 0 4px 4px 0;
}

.sub-nav-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.4;
  flex-shrink: 0;
}

.sub-nav-item.active .sub-nav-dot {
  opacity: 1;
}

.sub-nav-text {
  font-size: 13px;
  font-weight: 500;
}

/* 子菜单展开/收起动画 */
.submenu-enter-active,
.submenu-leave-active {
  transition: all 0.25s ease;
}

.submenu-enter-from,
.submenu-leave-to {
  opacity: 0;
  max-height: 0;
}

.submenu-enter-to,
.submenu-leave-from {
  opacity: 1;
  max-height: 300px;
}

/* =========================================================
 * 每日小贴士区域
 * ========================================================= */
.daily-tip-section {
  flex-shrink: 0;
  padding: 0 16px 16px;
  height: 340px;
  margin-bottom: var(--space-5);
  margin-top: var(--space-3);
}

.daily-tip-card {
  height: 100%;
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  opacity: 0.9;

  &:hover {
    box-shadow: var(--shadow-md);
  }
}

/* 加载状态 */
.tip-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-tertiary);
}

.tip-loading-spinner {
  width: 28px;
  height: 28px;
  border: 2px solid var(--primary-10);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: tip-spin 0.8s linear infinite;
}

@keyframes tip-spin {
  to {
    transform: rotate(360deg);
  }
}

.tip-loading-text {
  font-size: 13px;
}

/* 头部 */
.tip-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.tip-emoji {
  font-size: 28px;
  line-height: 1;
}

.tip-type-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 20px;
  letter-spacing: 0.5px;
}

/* 标题 */
.tip-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px;
  line-height: 1.4;
}

/* 正文 */
.tip-content {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin: 0;
  flex: 1;
  overflow-y: auto;
}

/* 谜语答案 */
.tip-riddle-answer {
  margin-top: 12px;
}

.tip-answer-btn {
  width: 100%;
  padding: 8px 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--primary-color);
  background: var(--primary-10);
  border: 1px dashed var(--primary-30);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    background: var(--primary-light);
    transform: translateY(-1px);
  }
}

.tip-answer-text {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-purple-500);
  background: rgba(179, 157, 219, 0.12);
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  margin: 0;
  text-align: center;
}

/* 底部日期 */
.tip-footer {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border-color);
  text-align: right;
}

.tip-date {
  font-size: 11px;
  color: var(--text-tertiary);
}

/* 空状态 */
.tip-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-tertiary);
}

.tip-empty-icon {
  font-size: 32px;
  opacity: 0.6;
}

.tip-empty-text {
  font-size: 13px;
}

/* 淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-sidebar {
    width: var(--sidebar-width-md);
  }

  .nav-link {
    padding: 10px 16px;
    gap: 12px;
  }

  .nav-text {
    font-size: 13px;
  }

  .sub-nav-link {
    padding: 8px 16px 8px 44px;
  }

  .daily-tip-section {
    height: 300px;
    padding: 0 12px 12px;
  }

  .daily-tip-card {
    padding: 16px;
  }

  .tip-emoji {
    font-size: 24px;
  }

  .tip-title {
    font-size: 14px;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .app-sidebar {
    width: 100%;
    position: relative;
    top: 0;
    height: auto;
    box-shadow: none;
  }

  .daily-tip-section {
    height: auto;
    min-height: 280px;
  }
}

/* 滚动条样式 */
.app-sidebar::-webkit-scrollbar {
  width: 6px;
}

.app-sidebar::-webkit-scrollbar-track {
  background: var(--primary-10);
}

.app-sidebar::-webkit-scrollbar-thumb {
  background: var(--primary-30);
  border-radius: 3px;
}

.app-sidebar::-webkit-scrollbar-thumb:hover {
  background: var(--primary-40);
}

/* 小贴士内部滚动条 */
.tip-content::-webkit-scrollbar {
  width: 4px;
}

.tip-content::-webkit-scrollbar-track {
  background: transparent;
}

.tip-content::-webkit-scrollbar-thumb {
  background: var(--primary-20);
  border-radius: 2px;
}

.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: var(--primary-20);
  border-radius: 2px;
}
</style>
