<template>
  <transition name="badge-toast" @after-leave="handleClose">
    <div v-show="visible" class="badge-toast">
      <div class="badge-toast-glow"/>
      <div class="badge-toast-content">
        <div class="badge-toast-icon">
          <i class="iconfont" :class="iconClass || 'icon-Trophy'"/>
        </div>
        <div class="badge-toast-text">
          <div class="badge-toast-title">解锁徽章</div>
          <div class="badge-toast-name">{{ name }}</div>
          <div v-if="expReward > 0" class="badge-toast-exp">+{{ expReward }} EXP</div>
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
  code: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: ''
  },
  iconClass: {
    type: String,
    default: ''
  },
  expReward: {
    type: Number,
    default: 0
  },
  duration: {
    type: Number,
    default: 3000
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

setTimeout(() => {
  close()
}, props.duration)
</script>

<style scoped lang="scss">
.badge-toast {
  position: relative;
  min-width: 200px;
  max-width: 300px;
  background: var(--badge-toast-bg);
  border: 1px solid var(--badge-toast-border);
  border-radius: var(--radius-md);
  box-shadow: var(--badge-toast-shadow);
  backdrop-filter: blur(10px);
  overflow: hidden;
  animation: badge-toast-in 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.badge-toast-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
      circle,
      rgba(139, 92, 246, 0.14) 0%,
      transparent 70%
  );
  pointer-events: none;
}

.badge-toast-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
}

.badge-toast-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(139, 92, 246, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  animation: badge-pop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);

  .iconfont {
    font-size: 24px;
    color: var(--badge-toast-icon);
  }
}

.badge-toast-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.badge-toast-title {
  font-size: 12px;
  color: var(--badge-toast-sub);
  line-height: 1.2;
}

.badge-toast-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--badge-toast-text);
  line-height: 1.3;
}

.badge-toast-exp {
  font-size: 13px;
  font-weight: 600;
  color: var(--badge-toast-exp);
}

.badge-toast-leave-active {
  transition: all 0.3s ease;
}

.badge-toast-leave-to {
  opacity: 0;
  transform: translateX(100%) scale(0.9);
}

@media (max-width: 768px) {
  .badge-toast {
    min-width: 180px;
    max-width: 260px;
  }

  .badge-toast-content {
    padding: var(--space-2) var(--space-3);
  }

  .badge-toast-icon {
    width: 40px;
    height: 40px;

    .iconfont {
      font-size: 22px;
    }
  }

  .badge-toast-name {
    font-size: 14px;
  }
}
</style>
