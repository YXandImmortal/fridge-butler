<template>
  <transition name="exp-toast" @after-leave="handleClose">
    <div v-show="visible" class="exp-toast">
      <div class="exp-toast-glow"/>
      <div class="exp-toast-content">
        <div class="exp-toast-icon">
          <i class="iconfont icon-star"/>
        </div>
        <div class="exp-toast-text">
          <div class="exp-toast-value">+{{ exp }} EXP</div>
          <div v-if="description" class="exp-toast-description">{{ description }}</div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import {ref, onMounted} from 'vue'

const props = defineProps({
  id: {
    type: String,
    default: ''
  },
  exp: {
    type: Number,
    default: 0
  },
  description: {
    type: String,
    default: ''
  },
  duration: {
    type: Number,
    default: 2500
  },
  onClose: {
    type: Function,
    default: null
  }
})

const visible = ref(false)

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

// 供外部调用
setTimeout(() => {
  close()
}, props.duration)
</script>

<style scoped lang="scss">
.exp-toast {
  position: relative;
  min-width: 180px;
  max-width: 280px;
  background: var(--exp-toast-bg);
  border: 1px solid var(--exp-toast-border);
  border-radius: var(--radius-md);
  box-shadow: var(--exp-toast-shadow);
  backdrop-filter: blur(10px);
  overflow: hidden;
  animation: exp-toast-in 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.exp-toast-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
      circle,
      rgba(245, 158, 11, 0.12) 0%,
      transparent 70%
  );
  pointer-events: none;
}

.exp-toast-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
}

.exp-toast-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(245, 158, 11, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  animation: exp-pop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);

  .iconfont {
    font-size: 20px;
    color: var(--exp-toast-icon);
  }
}

.exp-toast-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.exp-toast-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--exp-toast-text);
  line-height: 1.2;
}

.exp-toast-description {
  font-size: 12px;
  color: var(--exp-toast-desc);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.exp-toast-leave-active {
  transition: all 0.3s ease;
}

.exp-toast-leave-to {
  opacity: 0;
  transform: translateX(100%) scale(0.9);
}

@media (max-width: 768px) {
  .exp-toast {
    min-width: 160px;
    max-width: 240px;
  }

  .exp-toast-content {
    padding: var(--space-2) var(--space-3);
  }

  .exp-toast-icon {
    width: 32px;
    height: 32px;

    .iconfont {
      font-size: 18px;
    }
  }

  .exp-toast-value {
    font-size: 15px;
  }

  .exp-toast-description {
    font-size: 11px;
  }
}
</style>
