<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="confirm-dialog-overlay" @click.self="handleCancel">
      <div class="confirm-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-alert dialog-icon"></i>
            <h3 class="dialog-title">{{ title }}</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleCancel"></i>
        </div>
        <div class="dialog-content">
          <p>{{ message }}</p>
        </div>
        <div class="dialog-footer">
          <button class="dialog-btn dialog-btn-cancel" @click="handleCancel">{{ cancelText }}</button>
          <button class="dialog-btn dialog-btn-confirm" @click="handleConfirm">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '确认操作'
  },
  message: {
    type: String,
    default: '您确定要执行此操作吗？'
  },
  confirmText: {
    type: String,
    default: '确认'
  },
  cancelText: {
    type: String,
    default: '取消'
  }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel'])

const handleConfirm = () => {
  emit('confirm')
  emit('update:visible', false)
}

const handleCancel = () => {
  emit('cancel')
  emit('update:visible', false)
}
</script>

<style scoped>
.confirm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.confirm-dialog {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  max-width: 300px;
  width: 90%;
  animation: dialog-slide-in 0.3s ease-out;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 0;
}

.dialog-title-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dialog-icon {
  font-size: 24px;
  color: #f59e0b;
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.dialog-close {
  font-size: 20px !important;
  color: #a0aec0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dialog-close:hover {
  color: #64B5F6;
  transform: scale(1.1);
}

.dialog-content {
  padding: 24px;
}

.dialog-content p {
  margin: 0;
  font-size: 18px;
  color: var(--text-secondary);
  line-height: 1.5;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px 24px;
}

.dialog-btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.dialog-btn-cancel {
  background: #f7fafc;
  color: var(--text-secondary);
  border-color: #e2e8f0;
}

.dialog-btn-cancel:hover {
  background: #edf2f7;
  transform: translateY(-1px);
}

.dialog-btn-confirm {
  background: var(--primary-color);
  color: white;
}

.dialog-btn-confirm:hover {
  background: var(--primary-dark);
  transform: translateY(-1px);
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
  .confirm-dialog {
    min-width: 280px;
    width: 85%;
  }

  .dialog-header {
    padding: 16px 20px 0;
  }

  .dialog-icon {
    font-size: 20px;
  }

  .dialog-title {
    font-size: 18px;
  }

  .dialog-content {
    padding: 20px;
  }

  .dialog-footer {
    padding: 0 20px 20px;
  }
}
</style>