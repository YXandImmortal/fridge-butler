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
              <i v-if="feature.icon" class="iconfont" :class="feature.icon" />
              <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
              </svg>
            </div>
            <span class="nav-text">{{ feature.name }}</span>
            <!-- 展开箭头 -->
            <svg
              class="arrow-icon"
              :class="{ expanded: isExpanded(feature.id) }"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
            >
              <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
            </svg>
          </div>

          <!-- 无子菜单的情况：router-link 跳转 -->
          <router-link
            v-else
            :to="feature.path"
            class="nav-link"
          >
            <div class="nav-icon">
              <i v-if="feature.icon" class="iconfont" :class="feature.icon" />
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
  </aside>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useSystemStore } from "@/stores/system.js"
import { useRoute } from 'vue-router'

const systemStore = useSystemStore()
const { userIndexFeatures, getSystemInfo } = systemStore
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
}, { immediate: true })

onMounted(async () => {
  await getSystemInfo()
})
</script>

<style scoped lang="scss">
.app-sidebar {
  width: var(--sidebar-width);
  height: calc(100vh - var(--header-height));
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-sidebar);
  position: fixed;
  top: var(--header-height);
  left: 0;
  transition: all 0.3s ease;
  overflow-y: auto;
}

.nav-icon .iconfont {
  font-size: 22px;
}

.sidebar-nav {
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
</style>
