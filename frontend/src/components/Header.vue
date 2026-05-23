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
      <el-popover
        trigger="click"
        :width="420"
        popper-class="notification-popover"
        :teleported="true"
        @show="handlePopoverShow"
      >
        <template #reference>
          <div class="notification-icon">
            <el-badge :value="notificationStore.unreadCount" :hidden="!notificationStore.hasUnread" :max="99">
              <i class="iconfont icon-notification" />
            </el-badge>
          </div>
        </template>

        <!-- 下拉面板内容 -->
        <div class="notification-dropdown">
          <div class="dropdown-header">
            <span class="dropdown-title">消息通知</span>
            <div class="dropdown-actions">
              <CustomButton
                v-if="notificationStore.unreadCount > 0"
                type="link"
                size="small"
                @click="handleReadAll"
              >
                全部已读
              </CustomButton>
              <CustomButton
                type="link"
                size="small"
                @click="goToNotificationCenter"
              >
                查看全部
              </CustomButton>
            </div>
          </div>

          <!-- 最近未读消息列表 -->
          <div v-loading="dropdownLoading" class="dropdown-list">
            <template v-if="recentNotifications.length > 0">
              <div
                v-for="n in recentNotifications"
                :key="n.id"
                :class="['dropdown-item', { unread: n.status === 'UNREAD' }]"
                @click="handleNotificationClick(n)"
              >
                <div v-if="n.status === 'UNREAD'" class="dropdown-item-unread-bar" />
                <div class="dropdown-item-dot" :style="{ backgroundColor: getPriorityColor(n.priority) }" />
                <div class="dropdown-item-content">
                  <div class="dropdown-item-title">{{ n.title }}</div>
                  <div class="dropdown-item-time">{{ formatTime(n.createTime) }}</div>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无新消息" :image-size="60" class="dropdown-empty" />
          </div>
        </div>
      </el-popover>

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
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useUserStore } from "@/stores/user.js"
import { useSystemStore } from "@/stores/system.js"
import { useNotificationStore } from "@/stores/notification.js"
import { getNotificationList, markAllAsRead, markAsRead } from "@/api/notification.js"
import Logo from './Logo.vue'
import Avatar from './Avatar.vue'
import ThemeToggle from './ThemeToggle.vue'
import CustomButton from '@/components/CustomButton.vue'
import router from "@/router/index.js";

const userStore = useUserStore()
const systemStore = useSystemStore()
const notificationStore = useNotificationStore()
const { username, initUser } = userStore;
const { systemName, getSystemInfo } = systemStore;

// 使用computed确保头像响应式更新，直接从userStore访问avatar状态
const currentAvatar = computed(() => userStore.avatar);

// 控制下拉箭头旋转
const showUserMenu = ref(false);

// 定义事件
const emit = defineEmits(['show-logout-dialog']);

// 下拉面板相关
const dropdownLoading = ref(false)
const recentNotifications = ref([])

// 初始化用户信息和系统信息
onMounted(async () => {
  initUser()
  await getSystemInfo()
  notificationStore.init()
})

onUnmounted(() => {
  notificationStore.stopPolling()
})

// ========== 通知下拉面板 ==========
const handlePopoverShow = async () => {
  dropdownLoading.value = true
  try {
    const res = await getNotificationList({ status: 0, page: 1, size: 5 })
    if (res.code === 200) {
      recentNotifications.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
    }
    await notificationStore.fetchSummary()
  } catch (error) {
    console.error('加载最近消息失败:', error)
  } finally {
    dropdownLoading.value = false
  }
}

const getPriorityColor = (priority) => {
  switch (priority) {
    case 2: return 'var(--danger-color)'
    case 1: return 'var(--warn-color)'
    default: return 'var(--text-tertiary)'
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace(' ', 'T'))
  const now = new Date()
  const diff = now - date
  const oneMinute = 60 * 1000
  const oneHour = 60 * oneMinute
  const oneDay = 24 * oneHour

  if (diff < oneMinute) return '刚刚'
  if (diff < oneHour) return `${Math.floor(diff / oneMinute)} 分钟前`
  if (diff < oneDay && date.getDate() === now.getDate()) {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }
  if (diff < 2 * oneDay && date.getDate() === now.getDate() - 1) {
    return `昨天`
  }
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const handleNotificationClick = async (notification) => {
  // 标记已读
  if (notification.status === 'UNREAD') {
    await markAsRead(notification.id)
    notification.status = 'READ'
    notificationStore.fetchUnreadCount()
    notificationStore.fetchSummary()
  }
  // 跳转
  const route = notificationStore.getActionRoute(notification)
  if (route) {
    router.push(route)
  } else {
    goToNotificationCenter()
  }
}

const handleReadAll = async () => {
  await markAllAsRead()
  notificationStore.fetchUnreadCount()
  notificationStore.fetchSummary()
  recentNotifications.value.forEach(n => n.status = 'READ')
}

const goToNotificationCenter = () => {
  router.push('/notification/list')
}

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
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.notification-icon:hover {
  color: var(--primary-color);
}

.notification-icon:hover :deep(.el-badge) {
  transform: scale(1.1);
  transition: transform 0.3s ease;
}

.notification-icon .iconfont {
  width: 30px;
  height: 30px;
  font-size: 30px;
  display: inline-block;
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

/* ========== 通知下拉面板样式 ========== */
.notification-dropdown {
  padding: 0;
}

.dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--border-color);
}

.dropdown-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.dropdown-actions {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.dropdown-list {
  max-height: 200px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: var(--space-2) 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--gray-30);
    transform: translateX(2px);
  }

  &.unread {
    background: transparent;

    .dropdown-item-title {
      font-weight: 600;
      color: var(--text-primary);
    }
  }
}

.dropdown-item-unread-bar {
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: var(--primary-color);
  border-radius: 0 2px 2px 0;
}

.dropdown-item-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dropdown-item-content {
  flex: 1;
  min-width: 0;
}

.dropdown-item-title {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.dropdown-item-time {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.dropdown-empty {
  padding: var(--space-6) 0;
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
