<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="guide-type-dialog-overlay" @click.self="handleOverlayClick">
      <div class="guide-type-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-book-open dialog-icon"/>
            <h3 class="dialog-title">{{ title }}</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>
        <div class="dialog-content">
          <div class="guide-mode-group">
            <button
                type="button"
                class="guide-mode-btn"
                :class="{ 'is-active': modelValue === 'quick' }"
                @click="handleModeChange('quick')"
            >
              <i class="iconfont icon-teach"/>
              <span>快速指引</span>
            </button>
            <button
                type="button"
                class="guide-mode-btn"
                :class="{ 'is-active': modelValue === 'page' }"
                @click="handleModeChange('page')"
            >
              <i class="iconfont icon-layout"/>
              <span>页面指引</span>
            </button>
          </div>
        </div>
        <div class="dialog-footer">
          <CustomButton @click="handleClose">
            取消
          </CustomButton>
          <CustomButton type="primary" @click="handleConfirm">
            确认
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import CustomButton from '@/components/CustomButton.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '选择指引类型'
  },
  modelValue: {
    type: String,
    default: 'page'
  }
})

const emit = defineEmits(['update:visible', 'update:modelValue', 'confirm'])

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  handleClose()
}

const handleModeChange = (mode) => {
  emit('update:modelValue', mode)
}

const handleConfirm = () => {
  emit('confirm', props.modelValue)
  handleClose()
}
</script>

<style lang="scss" scoped>
.guide-type-dialog-overlay {
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

.guide-type-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 400px;
  width: 90%;
  animation: dialog-slide-in 0.3s ease-out;
  transition: background-color 0.3s ease, color 0.3s ease,
  border-color 0.3s ease, box-shadow 0.3s ease;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6) 0;
}

.dialog-title-container {
  display: flex;
  align-items: center;
  gap: var(--space-2) var(--space-3);
}

.dialog-icon {
  font-size: 24px;
  color: var(--primary-color);
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

.dialog-content {
  padding: var(--space-5) var(--space-6);
}

.guide-mode-group {
  display: flex;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.guide-mode-btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: transparent;
  color: var(--text-secondary);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  outline: none;
  white-space: nowrap;
  user-select: none;

  /* 中间分隔线：左侧按钮右边框 */
  &:not(:last-child) {
    border-right: 1px solid var(--border-color);
  }

  /* 图标字号 */
  .iconfont {
    font-size: 16px;
  }

  &:hover:not(.is-active) {
    background: var(--primary-10);
    color: var(--primary-color);
  }

  &.is-active {
    background: var(--primary-color);
    color: var(--text-inverse);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: 0 var(--space-6) var(--space-6);
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
  .guide-type-dialog {
    min-width: 280px;
    width: 85%;
  }

  .dialog-header {
    padding: var(--space-4) var(--space-5) 0;
  }

  .dialog-icon {
    font-size: 20px;
  }

  .dialog-title {
    font-size: 18px;
  }

  .dialog-content {
    padding: var(--space-4) var(--space-5);
  }

  .dialog-footer {
    padding: 0 var(--space-5) var(--space-5);
  }
}
</style>
