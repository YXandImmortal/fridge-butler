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
      <!-- 主题切换 -->
      <ThemeToggle />

      <!-- 通知图标 -->
      <div class="notification-icon">
        <el-badge :value="3">
          <i class="iconfont icon-notification" />
        </el-badge>
      </div>

      <!-- 用户信息 -->
      <el-dropdown trigger="hover" @command="handleCommand" @visible-change="(visible) => showUserMenu = visible">
        <div class="user-info">
          <div class="user-avatar">
            <Avatar :avatar-id="currentAvatar" size="small" />
          </div>
          <span class="user-name">{{ username }}</span>
          <i class="iconfont icon-chevron-down user-arrow" :class="{ 'rotate-180': showUserMenu }" />
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="userCenter">
              <span>个人中心</span>
              <i class="iconfont icon-user" style="margin-left: 8px;" />
            </el-dropdown-item>
            <el-dropdown-item command="logout" style="color: var(--danger-color)">
              <span>退出登录</span>
              <i class="iconfont icon-logout" style="margin-left: 8px;" />
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useUserStore } from "@/stores/user.js"
import { useSystemStore } from "@/stores/system.js"
import Logo from './Logo.vue'
import Avatar from './Avatar.vue'
import ThemeToggle from './ThemeToggle.vue'
import router from "@/router/index.js";

const userStore = useUserStore()
const systemStore = useSystemStore()
const { username, initUser } = userStore;
const { systemName, getSystemInfo } = systemStore;

// 使用computed确保头像响应式更新，直接从userStore访问avatar状态
const currentAvatar = computed(() => userStore.avatar);

// 控制下拉箭头旋转
const showUserMenu = ref(false);

// 定义事件
const emit = defineEmits(['show-logout-dialog']);

// 初始化用户信息和系统信息
onMounted(async () => {
  initUser()
  await getSystemInfo()
})

// 下拉菜单命令处理
const handleCommand = (command) => {
  if (command === 'userCenter') {
    router.push('/user/center');
  } else if (command === 'logout') {
    emit('show-logout-dialog');
  }
}

</script>

<style scoped lang="scss">
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: var(--header-height);
  padding: 0 24px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-header);
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
}

.notification-icon:hover .iconfont {
  transform: scale(1.1);
}

.notification-icon .iconfont {
  width: 30px;
  height: 30px;
  font-size: 30px;
  display: inline-block;
  transition: transform 0.3s ease;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
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
  font-size: var(--space-5);
  color: var(--text-secondary);
  transition: transform 0.3s ease;
}

.user-info:hover .user-arrow,
.rotate-180 {
  transform: rotate(180deg);
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
