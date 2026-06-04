<template>
  <transition name="dialog-fade">
    <div v-if="modelValue" class="notice-dialog-overlay" @click.self="handleClose">
      <div class="notice-dialog">
        <!-- 头部 -->
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-megaphone dialog-icon"/>
            <h3 class="dialog-title">{{ notice?.title || '重要通知' }}</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>

        <!-- 元信息 -->
        <div class="dialog-meta">
          <el-tag type="danger" size="small" effect="dark" class="type-tag">重要通知</el-tag>
          <span class="dialog-time">{{ formatTime(notice?.createTime) }}</span>
        </div>

        <!-- 内容区 -->
        <div class="dialog-content">
          <div class="markdown-body">
            <vue-markdown-render :source="notice?.content || ''"/>
          </div>
        </div>

        <!-- 底部 -->
        <div class="dialog-footer">
          <CustomButton type="primary" @click="handleConfirm">
            <i class="iconfont icon-check" style="margin-right: 6px;"/>
            我知道了
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import VueMarkdownRender from 'vue-markdown-render'
import CustomButton from '@/components/ui/CustomButton.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  notice: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'close', 'confirm'])

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace(' ', 'T'))
  const now = new Date()
  const isToday = date.getDate() === now.getDate() &&
    date.getMonth() === now.getMonth() &&
    date.getFullYear() === now.getFullYear()

  const time = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  if (isToday) {
    return `今天 ${time}`
  }
  return `${date.getMonth() + 1}月${date.getDate()}日 ${time}`
}

const handleClose = () => {
  emit('update:modelValue', false)
  emit('close', props.notice?.id)
}

const handleConfirm = () => {
  emit('update:modelValue', false)
  emit('confirm', props.notice?.id)
}
</script>

<style scoped lang="scss">
.notice-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--overlay-bg);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.notice-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  width: 90%;
  max-width: 680px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  animation: dialog-slide-in 0.3s ease-out;
  transition: background-color 0.3s ease, color 0.3s ease,
  border-color 0.3s ease, box-shadow 0.3s ease;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6) var(--space-3);
  flex-shrink: 0;
}

.dialog-title-container {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.dialog-icon {
  font-size: 24px;
  color: var(--danger-color);
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.dialog-close {
  font-size: var(--space-5);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dialog-close:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

.dialog-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 0 var(--space-6) var(--space-3);
  flex-shrink: 0;
}

.type-tag {
  font-weight: 500;
}

.dialog-time {
  font-size: 13px;
  color: var(--text-tertiary);
}

.dialog-content {
  padding: 0 var(--space-6);
  overflow-y: auto;
  flex: 1;
  min-height: 0;

  // 自定义滚动条
  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--primary-20);
    border-radius: 2px;
  }
}

.markdown-body {
  padding-bottom: var(--space-4);

  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4) {
    margin-top: var(--space-5);
    margin-bottom: var(--space-3);
    color: var(--text-primary);
    font-weight: 600;
    line-height: 1.4;
  }
  :deep(h1) { font-size: 20px; }
  :deep(h2) { font-size: 18px; }
  :deep(h3) { font-size: 16px; }
  :deep(h4) { font-size: 15px; }

  :deep(p) {
    margin: var(--space-3) 0;
    line-height: 1.8;
    color: var(--text-secondary);
  }

  :deep(ul),
  :deep(ol) {
    margin: var(--space-3) 0;
    padding-left: var(--space-6);
  }

  :deep(li) {
    margin: var(--space-1) 0;
    line-height: 1.7;
    color: var(--text-secondary);
  }

  :deep(a) {
    color: var(--primary-color);
    text-decoration: none;
    &:hover {
      text-decoration: underline;
    }
  }

  :deep(code) {
    background: var(--primary-10);
    padding: 2px 6px;
    border-radius: var(--radius-sm);
    font-family: 'Fira Code', 'Consolas', monospace;
    font-size: 13px;
    color: var(--primary-color);
  }

  :deep(pre) {
    background: var(--input-bg);
    padding: var(--space-4);
    border-radius: var(--radius-md);
    overflow-x: auto;
    margin: var(--space-3) 0;

    code {
      background: transparent;
      padding: 0;
      color: var(--text-secondary);
    }
  }

  :deep(blockquote) {
    margin: var(--space-3) 0;
    padding: var(--space-3) var(--space-4);
    border-left: 4px solid var(--primary-color);
    background: var(--primary-10);
    border-radius: 0 var(--radius-sm) var(--radius-sm) 0;

    p {
      margin: 0;
    }
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: var(--space-3) 0;
    font-size: 14px;

    th, td {
      padding: var(--space-2) var(--space-3);
      border: 1px solid var(--border-color);
      text-align: left;
    }

    th {
      background: var(--primary-10);
      font-weight: 600;
      color: var(--text-primary);
    }

    tr:nth-child(even) {
      background: var(--glass-bg);
    }
  }

  :deep(hr) {
    border: none;
    border-top: 1px solid var(--border-color);
    margin: var(--space-5) 0;
  }

  :deep(img) {
    max-width: 100%;
    border-radius: var(--radius-sm);
    margin: var(--space-3) 0;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6) var(--space-5);
  flex-shrink: 0;
  border-top: 1px solid var(--border-color);
}

@keyframes dialog-slide-in {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s ease;
}

.dialog-fade-enter-from {
  opacity: 0;
}

.dialog-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .notice-dialog {
    width: 92%;
    max-height: 88vh;
  }

  .dialog-header {
    padding: var(--space-4) var(--space-5) var(--space-2);
  }

  .dialog-icon {
    font-size: 20px;
  }

  .dialog-title {
    font-size: 18px;
  }

  .dialog-meta {
    padding: 0 var(--space-5) var(--space-2);
  }

  .dialog-content {
    padding: 0 var(--space-5);
  }

  .dialog-footer {
    padding: var(--space-3) var(--space-5) var(--space-4);
  }

  .markdown-body {
    :deep(h1) { font-size: 18px; }
    :deep(h2) { font-size: 16px; }
    :deep(h3) { font-size: 15px; }
  }
}
</style>
