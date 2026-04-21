<template>
  <header class="app-header">
    <!-- 左侧 Logo 和标题 -->
    <div class="header-left">
      <div class="logo-container">
        <div class="logo-icon">
          <Logo />
        </div>
        <h1 class="app-title">{{ systemName || '冰箱管理系统' }}</h1>
      </div>
    </div>

    <!-- 中间文本 -->
    <div class="header-center">
      <span class="header-center-text">引擎启动 · 新鲜常驻</span>
    </div>

    <!-- 右侧用户信息和操作 -->
    <div class="header-right">
      <!-- 通知图标 -->
      <div class="notification-icon">
        <i class="iconfont icon-notification" />
        <span class="notification-badge">3</span>
      </div>

      <!-- 用户信息 -->
      <div class="user-info" 
           @mouseenter="handleMouseEnter" 
           @mouseleave="handleMouseLeave"
           @click="toggleUserMenu"
           @keydown="handleKeydown"
           tabindex="0"
           role="button"
           :aria-expanded="showUserMenu">
        <div class="user-avatar">
          <Avatar :avatar-id="currentAvatar" size="small" />
        </div>
        <span class="user-name">{{ username }}</span>
        <i class="iconfont icon-chevron-down user-arrow" :class="{ 'rotate-180': showUserMenu }" />
        
        <!-- 下拉菜单 -->
        <div class="user-dropdown" 
             ref="dropdownRef" 
             v-show="showUserMenu" 
             @mouseenter="clearHideTimer"
             @mouseleave="startHideTimer">
          <div class="dropdown-item" 
               @click="goToUserCenter"
               @keydown.enter="goToUserCenter"
               tabindex="0"
               role="menuitem">个人中心<i class="iconfont icon-user" /></div>
          <div class="dropdown-item" 
               @click="showLogoutConfirm"
               @keydown.enter="showLogoutConfirm"
               tabindex="0"
               role="menuitem" style="color: var(--danger-color)">退出登录<i class="iconfont icon-logout" /></div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useUserStore } from "@/stores/user.js"
import { useSystemStore } from "@/stores/system.js"
import Logo from './Logo.vue'
import Avatar from './Avatar.vue'
import router from "@/router/index.js";

const userStore = useUserStore()
const systemStore = useSystemStore()
const { username, initUser } = userStore;
const { systemName, getSystemInfo } = systemStore;

// 使用computed确保头像响应式更新，直接从userStore访问avatar状态
const currentAvatar = computed(() => userStore.avatar);

// 控制下拉菜单显示/隐藏
const showUserMenu = ref(false);
const dropdownRef = ref(null);
let hideTimer = null;

// 定义事件
const emit = defineEmits(['show-logout-dialog']);

// 初始化用户信息和系统信息
onMounted(async () => {
  initUser()
  await getSystemInfo()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (hideTimer) {
    clearTimeout(hideTimer);
    hideTimer = null;
  }
})

// 鼠标进入处理
const handleMouseEnter = () => {
  clearHideTimer();
  showUserMenu.value = true;
}

// 鼠标离开处理
const handleMouseLeave = () => {
  startHideTimer();
}

// 清除隐藏定时器
const clearHideTimer = () => {
  if (hideTimer) {
    clearTimeout(hideTimer);
    hideTimer = null;
  }
}

// 开始隐藏定时器
const startHideTimer = () => {
  hideTimer = setTimeout(() => {
    showUserMenu.value = false;
  }, 300); // 300ms延迟，给用户足够时间移动鼠标到下拉菜单
}

// 切换用户菜单显示/隐藏
const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value;
  clearHideTimer();
}

// 键盘事件处理
const handleKeydown = (event) => {
  switch (event.key) {
    case 'Enter':
    case ' ':
      event.preventDefault();
      toggleUserMenu();
      break;
    case 'Escape':
      showUserMenu.value = false;
      break;
    case 'Tab':
      if (showUserMenu.value) {
        // 如果菜单打开，Tab键应该先聚焦到菜单项
        event.preventDefault();
        setTimeout(() => {
          const firstMenuItem = dropdownRef.value?.querySelector('.dropdown-item');
          firstMenuItem?.focus();
        }, 0);
      }
      break;
  }
}

// 个人中心
const goToUserCenter = () => {
  router.push('/user/center');
  showUserMenu.value = false;
}

// 显示登出确认对话框
const showLogoutConfirm = () => {
  showUserMenu.value = false;
  // 触发事件，通知父组件显示登出确认对话框
  emit('show-logout-dialog');
}

</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.iconfont.icon-avatar {
  font-size: 28px !important;
}

.logo-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.logo-icon:hover {
  transform: scale(1.05);
}

.logo-icon svg {
  width: 20px;
  height: 20px;
}

.app-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.header-center {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.header-center-text {
  letter-spacing: 15px;
  font-size: 26px;
  font-weight: 500;
  color: var(--text-secondary);
  opacity: 0.6;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.notification-icon {
  position: relative;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s ease;
  vertical-align: middle;
}

.notification-icon:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

.notification-icon i {
  width: 30px;
  height: 30px;
  font-size: 30px !important;
}

.notification-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: var(--danger-color);
  color: white;
  font-size: 12px;
  font-weight: 600;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-info:hover {
  transform: scale(1.04);
}

.user-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-arrow {
  font-size: 20px !important;
  color: var(--text-secondary);
  transition: transform 0.3s ease;
}

.user-info:hover .user-arrow,
.rotate-180 {
  transform: rotate(180deg);
}

.user-info:focus {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
  border-radius: 4px;
}

/* 下拉菜单样式 */
.user-dropdown {
  position: absolute;
  top: 49px;
  right: 24px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 110px;
  z-index: 1001;
  animation: dropdown-fade-in 0.3s ease;
  text-align: center;
}

@keyframes dropdown-fade-in {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  padding: 10px 16px;
  font-size: 14px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dropdown-item i {
  font-size: 14px !important;
}

.dropdown-item:hover {
  background: var(--primary-light);
}

.dropdown-item:first-child {
  border-radius: 4px 4px 0 0;
}

.dropdown-item:last-child {
  border-radius: 0 0 4px 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 16px;
  }

  .app-title {
    font-size: 18px;
  }

  .header-right {
    gap: 16px;
  }
}
</style>