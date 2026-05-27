<template>
  <div class="notification-page">
    <!-- 页面标题栏 -->
    <div class="page-header animate-in">
      <div class="page-header-left">
        <i class="iconfont icon-notification page-header-icon"/>
        <h2 class="page-title">消息中心</h2>
        <el-tag v-if="unreadCount > 0" type="danger" size="small" class="unread-tag">
          {{ unreadCount }} 条未读
        </el-tag>
      </div>
      <div class="page-header-actions">
        <CustomButton
            class="refresh-btn"
            @click="handleRefresh"
        >
          <i class="iconfont icon-reload"/>刷新
        </CustomButton>
        <CustomButton
            v-if="unreadCount > 0"
            type="primary"
            @click="handleReadAll"
        >
          <i class="iconfont icon-mail-check"/>全部已读
        </CustomButton>
      </div>
    </div>

    <!-- 主体区域：左侧筛选 + 右侧列表 -->
    <div class="notification-main">
      <!-- 左侧分类筛选面板 -->
      <aside class="category-panel animate-in" style="animation-delay: 0.08s">
        <div class="category-list">
          <div
              v-for="item in summaryItems"
              :key="item.key"
              :class="['category-card', { active: currentType === item.key }]"
              @click="handleTypeChange(item.key)"
          >
            <div class="category-icon-wrapper" :style="{ background: `${item.color}15` }">
              <i :class="['iconfont', item.icon]" :style="{ color: item.color }"/>
            </div>
            <div class="category-info">
              <div class="category-label">{{ item.label }}</div>
              <div class="category-count" :style="{ color: item.count > 0 ? item.color : 'var(--text-tertiary)' }">
                <span style="margin-right: 8px; font-weight: 200;">未读消息</span>{{ item.count }}
              </div>
            </div>
            <div v-if="currentType === item.key" class="active-indicator"/>
          </div>
        </div>
      </aside>

      <!-- 右侧消息列表 -->
      <main class="notification-list-panel animate-in" style="animation-delay: 0.16s">
        <!-- 加载中 -->
        <div v-if="loading && notificationList.length === 0" v-loading="true" class="loading-wrapper"/>

        <!-- 空状态 -->
        <el-empty
            v-else-if="notificationList.length === 0"
            description="暂无消息"
            class="notification-empty"
        >
          <template #image>
            <i class="iconfont icon-notification empty-icon"/>
          </template>
        </el-empty>

        <!-- 消息列表 -->
        <template v-else>
          <el-scrollbar height="600px" view-style="
          display: flex;
          flex-direction: column;
          gap: var(--space-3);
          padding-right: var(--space-4); ">
            <div
                v-for="(notification, index) in notificationList"
                :key="notification.id"
                :class="[
                'notification-card',
                { unread: notification.status === 'UNREAD' },
                { clickable: getActionRoute(notification) }
              ]"
                :style="{ animationDelay: `${0.2 + index * 0.05}s` }"
                @click="handleCardClick(notification)"
            >
              <!-- 左侧优先级色条 -->
              <div
                  class="priority-bar"
                  :style="{ backgroundColor: getPriorityColor(notification.priority) }"
              />

              <!-- 未读指示点 -->
              <div v-if="notification.status === 'UNREAD'" class="unread-dot"/>

              <!-- 消息内容区 -->
              <div class="notification-content">
                <div class="notification-header">
                  <el-tag
                      :type="getTagType(notification.type)"
                      size="small"
                      class="type-tag"
                      effect="light"
                  >
                    {{ notification.typeLabel }}
                  </el-tag>
                  <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
                </div>

                <h4 class="notification-title">{{ notification.title }}</h4>
                <p class="notification-body">{{ notification.content }}</p>
              </div>

              <!-- 操作按钮区 -->
              <div class="notification-actions">
                <CustomButton
                    v-if="notification.status === 'UNREAD'"
                    type="link"
                    size="small"
                    class="action-btn"
                    @click.stop="handleMarkRead(notification.id)"
                >
                  标为已读
                </CustomButton>
                <CustomButton
                    type="link"
                    size="small"
                    class="action-btn danger-link"
                    @click.stop="handleDelete(notification.id)"
                >
                  <i class="iconfont icon-trash"/>
                </CustomButton>
              </div>
            </div>
          </el-scrollbar>

        </template>
      </main>
    </div>
    <NotificationTour ref="tourRef"/>
  </div>
</template>

<script setup>
import NotificationTour from '@/components/tour/NotificationTour.vue'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'
import {onMounted, onUnmounted, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {Check, RefreshRight} from '@element-plus/icons-vue'
import CustomButton from '@/components/CustomButton.vue'
import {useNotificationStore} from '@/stores/notification'
import showMessage from '@/utils/message'

const router = useRouter()
const notificationStore = useNotificationStore()

const {
  unreadCount,
  summaryItems,
  notificationList,
  currentType,
  loading
} = storeToRefs(notificationStore)

const {
  fetchList,
  readOne,
  readAll,
  removeOne,
  getActionRoute,
  init,
  stopPolling
} = notificationStore

// ========== 优先级颜色 ==========
const getPriorityColor = (priority) => {
  switch (priority) {
    case 2:
      return 'var(--danger-color)'
    case 1:
      return 'var(--warn-color)'
    default:
      return 'var(--text-tertiary)'
  }
}

// ========== Element Plus Tag 类型映射 ==========
const getTagType = (type) => {
  switch (type) {
    case 'EXPIRED':
    case 'EXPIRING_CRITICAL':
      return 'danger'
    case 'EXPIRING_WARNING':
    case 'CAPACITY_WARNING':
      return 'warning'
    case 'EXPIRING_NOTICE':
      return 'info'
    case 'SYSTEM':
      return undefined
    default:
      return 'info'
  }
}

// ========== 时间格式化 ==========
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
    return `今天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }
  if (diff < 2 * oneDay && date.getDate() === now.getDate() - 1) {
    return `昨天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// ========== 事件处理 ==========
const handleTypeChange = (type) => {
  fetchList(type)
}


const handleRefresh = () => {
  fetchList(currentType.value)
  notificationStore.fetchSummary()
  notificationStore.fetchUnreadCount()
  showMessage.success('刷新成功')
}

const handleMarkRead = async (id) => {
  const success = await readOne(id)
  if (success) {
    showMessage.success('已标记为已读')
  }
}

const handleReadAll = async () => {
  const success = await readAll()
  if (success) {
    showMessage.success('已全部标记为已读')
  }
}

const handleDelete = async (id) => {
  const success = await removeOne(id)
  if (success) {
    showMessage.success('已删除')
  }
}

const handleCardClick = async (notification) => {
  // 如果是未读，先标记已读
  if (notification.status === 'UNREAD') {
    await readOne(notification.id)
  }

  // 有跳转则跳转
  const route = getActionRoute(notification)
  if (route) {
    router.push(route)
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  init()
  fetchList()
})

onUnmounted(() => {
  stopPolling()
})
// 页面引导
const tourRef = ref(null)
const tourStore = useTourStore()

watch(() => tourStore.pendingStartScene, (scene) => {
  if (scene === TOUR_SCENES.NOTIFICATION) {
    tourRef.value?.start()
  }
})
</script>

<style scoped lang="scss">
.notification-page {
  padding: var(--space-5);
  min-height: calc(100vh - var(--header-height) - var(--footer-height) - var(--space-5) * 2);
}

/* ========== 页面标题栏 ========== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-color);
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-header-icon {
  font-size: 28px;
  color: var(--primary-color);
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.unread-tag {
  font-weight: 500;
}

.page-header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.refresh-btn {
  color: var(--text-secondary);
}

/* ========== 主体区域 ========== */
.notification-main {
  display: flex;
  gap: var(--space-5);
}

/* ========== 左侧分类面板 ========== */
.category-panel {
  width: 220px;
  flex-shrink: 0;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.category-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--card-bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &:hover {
    transform: translateX(4px);
    box-shadow: var(--shadow-sm);
  }

  &.active {
    border-color: var(--primary-color);
    background: var(--primary-light);
  }
}

.active-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--primary-color);
  border-radius: 0 4px 4px 0;
}

.category-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .iconfont {
    font-size: 20px;
  }
}

.category-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.category-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.category-count {
  font-size: 13px;
  font-weight: 600;
  transition: color 0.3s ease;
}

/* ========== 右侧列表面板 ========== */
.notification-list-panel {
  flex: 1;
  min-width: 0;
}

.loading-wrapper {
  min-height: 300px;
}

.notification-empty {
  padding: var(--space-10) 0;

  .empty-icon {
    font-size: 64px;
    color: var(--text-tertiary);
    opacity: 0.4;
  }
}

/* ========== 消息卡片 ========== */

.notification-card {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--card-bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  position: relative;
  transition: all 0.3s ease;
  overflow: hidden;

  &.unread {
    background: var(--primary-light);
    border-color: var(--border-light);
  }

  &.clickable {
    cursor: pointer;
  }

  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);

    .notification-actions {
      opacity: 1;
    }
  }
}

.priority-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  border-radius: var(--radius-md) 0 0 var(--radius-md);
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-color);
  flex-shrink: 0;
  margin-top: 6px;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(0.85);
  }
}

.notification-content {
  flex: 1;
  min-width: 0;
  padding-left: 4px;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.type-tag {
  font-weight: 500;
}

.notification-time {
  font-size: 12px;
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
  line-height: 1.4;
}

.notification-body {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ========== 操作按钮 ========== */
.notification-actions {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  opacity: 0;
  transition: opacity 0.3s ease;
  flex-shrink: 0;
}

.action-btn {
  padding: 4px 8px;

  .iconfont {
    font-size: 14px;
  }
}

.notification-actions .danger-link {
  color: var(--danger-color);
}

.notification-actions .danger-link:hover {
  color: var(--danger-dark);
}

/* ========== 分页 ========== */
.pagination-wrapper {
  margin-top: var(--space-6);
  display: flex;
  justify-content: flex-end;
}

/* ========== 动画 ========== */
.animate-in {
  animation: fade-in-up 0.5s ease-out both;
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .notification-main {
    flex-direction: column;
  }

  .category-panel {
    width: 100%;
  }

  .category-list {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .category-card {
    flex: 1;
    min-width: 140px;
  }

  .notification-actions {
    opacity: 1;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }
}
</style>
