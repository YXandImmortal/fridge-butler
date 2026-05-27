<template>
  <el-drawer
      :model-value="visible"
      title="会话列表"
      direction="ltr"
      size="320px"
      class="session-drawer"
      :with-header="true"
      @update:model-value="$emit('update:visible', $event)"
  >
    <div class="session-drawer-content">
      <button class="new-session-btn" @click="$emit('new-session')">
        <i class="iconfont icon-add-box"/>
        <span>新建会话</span>
      </button>

      <div v-loading="sessionLoading" class="session-list">
        <div
            v-for="session in sessions"
            :key="session.sessionId"
            :class="['session-item', session.sessionId === sessionId ? 'session-item-active' : '']"
            @click="$emit('switch-session', session.sessionId)"
        >
          <div class="session-item-main">
            <div class="session-title">{{ session.title || '新会话' }}</div>
            <div class="session-time">{{ formatSessionTime(session.lastActiveTime) }}</div>
          </div>
          <button
              class="session-delete-btn"
              title="删除会话"
              @click.stop="$emit('delete-session', session.sessionId)"
          >
            <i class="iconfont icon-trash"/>
          </button>
        </div>

        <el-empty v-if="sessions.length === 0 && !sessionLoading" description="暂无会话记录"/>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  sessionId: {
    type: String,
    default: null
  },
  sessions: {
    type: Array,
    default: () => []
  },
  sessionLoading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:visible', 'new-session', 'switch-session', 'delete-session'])

function formatSessionTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const oneDay = 24 * 60 * 60 * 1000

  if (diff < oneDay && date.getDate() === now.getDate()) {
    return `今天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }
  if (diff < 2 * oneDay && date.getDate() === now.getDate() - 1) {
    return `昨天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped lang="scss">
.session-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: var(--space-4) var(--space-5);
    border-bottom: 1px solid var(--border-color);
    color: var(--text-primary);
    font-weight: 600;
  }

  :deep(.el-drawer__body) {
    padding: 0;
    background: var(--main-content-bg);
  }
}

.session-drawer-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: var(--space-4);
}

.new-session-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  border: 1px dashed var(--border-color);
  background: var(--glass-bg);
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: var(--space-4);

  &:hover {
    border-color: var(--primary-color);
    color: var(--primary-color);
    background: var(--primary-light);
  }

  .iconfont {
    font-size: 18px;
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-2);
}

.session-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--glass-bg);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: var(--shadow-sm);
    border-color: var(--primary-color);
    transform: translateY(-2px);
  }
}

.session-item-active {
  border-color: var(--primary-color);
  background: var(--primary-light);

  .session-title {
    color: var(--primary-dark);
    font-weight: 600;
  }
}

.session-item-main {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.session-title {
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.session-delete-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  opacity: 0;

  .session-item:hover & {
    opacity: 1;
  }

  &:hover {
    background: var(--danger-light);
    color: var(--danger-color);
  }

  .iconfont {
    font-size: 14px;
  }
}
</style>
