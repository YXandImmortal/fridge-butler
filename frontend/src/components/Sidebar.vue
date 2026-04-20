<template>
  <aside class="app-sidebar">
    <!-- 导航菜单 -->
    <nav class="sidebar-nav">
      <ul class="nav-menu">
        <li v-for="feature in userIndexFeatures" :key="feature.id" :class="['nav-item', { active: isActive(feature.path) }]">
          <router-link :to="feature.path" class="nav-link">
            <div class="nav-icon">
              <!-- 使用Element Plus图标 -->
              <i v-if="feature.icon" class="iconfont" :class="feature.icon" />
              <!--  fallback到默认图标 -->
              <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
              </svg>
            </div>
            <span class="nav-text">{{ feature.name }}</span>
          </router-link>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
import { onMounted } from 'vue'
import { useSystemStore } from "@/stores/system.js"
import { useRoute } from 'vue-router'

const systemStore = useSystemStore()
const { userIndexFeatures, getSystemInfo } = systemStore
const route = useRoute()

// 检查当前路径是否与菜单项路径匹配
const isActive = (path) => {
  return route.path === path
}

onMounted(async () => {
  await getSystemInfo()
})
</script>

<style scoped>
.app-sidebar {
  width: 240px;
  height: calc(100vh - 64px);
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 64px;
  left: 0;
  transition: all 0.3s ease;
  overflow-y: auto;
}

.iconfont {
  font-size: 22px !important;
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
  margin-bottom: 8px;
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
}

.nav-link:hover {
  background: var(--primary-10);
  color: var(--primary-color);
  transform: translateX(4px);
}

.nav-item.active .nav-link {
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 600;
}

.nav-item.active .nav-link::before {
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
}

.nav-icon svg {
  width: 20px;
  height: 20px;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-sidebar {
    width: 200px;
  }

  .nav-link {
    padding: 10px 16px;
    gap: 12px;
  }

  .nav-text {
    font-size: 13px;
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