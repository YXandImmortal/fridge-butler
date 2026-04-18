<template>
  <transition name="message-fade" @after-leave="handleClose">
    <div v-show="visible" :class="['custom-message', `custom-message--${type}`]">
      <div class="message-content">
        <i :class="['message-icon', 'iconfont', iconFontClass]"></i>
        <span class="message-text">{{ message }}</span>
        <i class="message-close iconfont icon-close" @click="close"></i>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  id: String,
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
  },
  message: String,
  duration: {
    type: Number,
    default: 3000
  },
  onClose: Function
})

const visible = ref(false)

const iconFontClass = computed(() => {
  const icons = {
    success: 'icon-checkbox',
    error: 'icon-close-box',
    warning: 'icon-warning-box',
    info: 'icon-info-box'
  }
  return icons[props.type]
})

onMounted(() => {
  visible.value = true
})

const close = () => {
  visible.value = false
}

const handleClose = () => {
  if (props.onClose) {
    props.onClose()
  }
}
</script>

<style scoped>
.custom-message {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  min-width: 320px;
  max-width: 600px;
  animation: slide-down 0.3s ease-out;
}

.message-content {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(100, 181, 246, 0.2);
}

.message-icon {
  font-size: 20px !important;
  flex-shrink: 0;
}

.icon-checkbox {
  color: #81D4FA;
}

.icon-close-box {
  color: #F87171;
}

.icon-warning-box {
  color: #FFB74D;
}

.icon-info-box {
  color: #64B5F6;
}

.message-text {
  flex: 1;
  font-size: 14px;
  color: #2d3748;
  line-height: 1.5;
}

.message-close {
  font-size: 16px !important;
  color: #a0aec0;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.message-close:hover {
  color: #64B5F6;
  transform: scale(1.1);
}

@keyframes slide-down {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.message-fade-enter-active,
.message-fade-leave-active {
  transition: all 0.3s ease;
}

.message-fade-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

.message-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

@media (max-width: 768px) {
  .custom-message {
    min-width: 280px;
    max-width: 90%;
  }

  .message-content {
    padding: 14px 16px;
  }

  .message-text {
    font-size: 13px;
  }
}
</style>