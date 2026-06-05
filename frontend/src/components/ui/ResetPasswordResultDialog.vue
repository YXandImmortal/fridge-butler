<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="reset-pwd-result-overlay" @click.self="handleOverlayClick">
      <div class="reset-pwd-result-dialog">
        <!-- 头部 -->
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-check dialog-icon"/>
            <h3 class="dialog-title">{{ title }}</h3>
          </div>
          <i v-if="showClose" class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>

        <!-- 内容区 -->
        <div class="dialog-content">
          <p class="result-text">
            用户 <strong>{{ username }}</strong> 的密码已重置为：
          </p>
          <div class="pwd-box">
            <code>{{ password }}</code>
            <CustomButton type="primary" size="small" @click="handleCopy">
              <i class="iconfont icon-copy" />
              复制
            </CustomButton>
          </div>
          <p class="result-hint">{{ hint }}</p>
        </div>

        <!-- 底部：仅确认按钮 -->
        <div class="dialog-footer">
          <CustomButton type="primary" @click="handleClose">
            {{ confirmText }}
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import CustomButton from './CustomButton.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '密码重置成功'
  },
  username: {
    type: String,
    default: ''
  },
  password: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: '请妥善保存，关闭后将无法再次查看。'
  },
  confirmText: {
    type: String,
    default: '我知道了'
  },
  showClose: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:visible', 'copy'])

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  // 密码结果对话框，点击遮罩不关闭，防止误触导致密码丢失
}

const handleCopy = () => {
  emit('copy', props.password)
}
</script>

<style scoped lang="scss">
.reset-pwd-result-overlay {
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

.reset-pwd-result-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 440px;
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
  color: var(--success-color);
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
  text-align: center;
}

.result-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 var(--space-4);

  strong {
    color: var(--text-primary);
  }
}

.pwd-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  background: var(--primary-light);
  border: 1px dashed var(--primary-30);
  border-radius: var(--radius-sm);
  padding: var(--space-4) var(--space-5);
  margin-bottom: var(--space-4);

  code {
    font-size: 18px;
    font-weight: 700;
    color: var(--primary-color);
    font-family: 'Courier New', monospace;
    letter-spacing: 1px;
  }
}

.result-hint {
  font-size: 12px;
  color: var(--text-tertiary);
  margin: 0;
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
  .reset-pwd-result-dialog {
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

  .pwd-box {
    padding: var(--space-3) var(--space-4);
    gap: var(--space-3);

    code {
      font-size: 16px;
    }
  }

  .dialog-footer {
    padding: 0 var(--space-5) var(--space-5);
  }
}
</style>
