<template>
  <transition name="level-up" @after-leave="handleClose">
    <div v-show="visible" class="level-up-notify">
      <!-- 冰鲜光晕背景 -->
      <div class="level-up-glow"/>

      <!-- 飘散雪花粒子 -->
      <div class="level-up-particles">
        <span v-for="n in 8" :key="n" class="particle" :class="`particle--${n}`">❄</span>
      </div>

      <!-- 内容区 -->
      <div class="level-up-content">
        <div class="level-up-icon">
          <i class="iconfont icon-level-snowflakes"/>
        </div>

        <div class="level-up-title">升级啦！</div>

        <div class="level-up-badge">
          <LevelBadge
              :level="level"
              :title="title"
              :total-exp="totalExp"
              :icons="icons"
          />
        </div>

        <div class="level-up-meta">
          <span class="level-up-level">Lv.{{ level }}</span>
          <span class="level-up-divider">·</span>
          <span class="level-up-name">{{ title }}</span>
        </div>

        <div class="level-up-exp">累计 EXP {{ totalExp }}</div>

        <div v-if="nextUnlockText" class="level-up-preview">
          <i class="iconfont icon-a-SheriffBadge"/>
          <span>{{ nextUnlockText }}</span>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import LevelBadge from './LevelBadge.vue'

const props = defineProps({
  id: {
    type: String,
    default: ''
  },
  level: {
    type: Number,
    default: 1
  },
  title: {
    type: String,
    default: ''
  },
  totalExp: {
    type: Number,
    default: 0
  },
  icons: {
    type: Object,
    default: () => ({})
  },
  nextLevel: {
    type: Object,
    default: null
  },
  duration: {
    type: Number,
    default: 4000
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

const nextUnlockText = computed(() => {
  if (!props.nextLevel) return ''

  const {level, title, icons} = props.nextLevel
  if (!level) return ''

  // 统计下一级新增的图标数量
  const iconLabels = []
  const iconNames = {
    snowman: '雪人',
    iceCream: '冰淇淋',
    ice: '冰块',
    snowflake: '雪花'
  }

  Object.entries(icons || {}).forEach(([key, count]) => {
    const currentCount = parseInt(props.icons?.[key], 10) || 0
    const nextCount = parseInt(count, 10) || 0
    const added = nextCount - currentCount
    if (added > 0) {
      iconLabels.push(`${iconNames[key] || key} ×${added}`)
    }
  })

  const parts = []
  if (title) {
    parts.push(`称号「${title}」`)
  }
  if (iconLabels.length > 0) {
    parts.push(iconLabels.join('、'))
  }

  if (parts.length === 0) {
    return `下一级 Lv.${level} 即将解锁新内容`
  }

  return `下一级 Lv.${level} 解锁：${parts.join('，')}`
})

setTimeout(() => {
  close()
}, props.duration)
</script>

<style scoped lang="scss">
.level-up-notify {
  position: fixed;
  top: var(--space-5);
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  min-width: 320px;
  max-width: 420px;
  background: linear-gradient(135deg, rgba(186, 230, 253, 0.95) 0%, rgba(255, 255, 255, 0.95) 100%);
  border: 1px solid rgba(147, 197, 253, 0.6);
  border-radius: var(--radius-lg);
  box-shadow:
      0 0 0 1px rgba(255, 255, 255, 0.5) inset,
      0 20px 50px rgba(59, 130, 246, 0.2),
      0 8px 24px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(12px);
  overflow: hidden;
  animation: level-up-in 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.level-up-glow {
  position: absolute;
  inset: -50%;
  background: radial-gradient(
      circle at center,
      rgba(147, 197, 253, 0.35) 0%,
      rgba(186, 230, 253, 0.15) 40%,
      transparent 70%
  );
  animation: level-up-glow 2s ease-in-out infinite;
  pointer-events: none;
}

.level-up-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  top: 50%;
  left: 50%;
  font-size: 14px;
  color: rgba(59, 130, 246, 0.7);
  opacity: 0;
  animation: particle-float 1.4s ease-out forwards;
}

.particle--1 { animation-delay: 0.1s; }
.particle--2 { animation-delay: 0.2s; }
.particle--3 { animation-delay: 0.3s; }
.particle--4 { animation-delay: 0.15s; }
.particle--5 { animation-delay: 0.25s; }
.particle--6 { animation-delay: 0.35s; }
.particle--7 { animation-delay: 0.2s; }
.particle--8 { animation-delay: 0.4s; }

.level-up-content {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-6) var(--space-6);
  text-align: center;
}

.level-up-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #93c5fd 0%, #bfdbfe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.25);
  animation: level-up-icon-pop 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) 0.15s both;

  .iconfont {
    font-size: 28px;
    color: #fff;
  }
}

.level-up-title {
  font-size: 22px;
  font-weight: 800;
  color: #1e40af;
  letter-spacing: 2px;
  animation: level-up-text-in 0.5s ease-out 0.25s both;
  text-shadow: 0 2px 8px rgba(147, 197, 253, 0.5);
}

.level-up-badge {
  margin: var(--space-1) 0;
  padding: var(--space-2) var(--space-4);
  background: rgba(255, 255, 255, 0.6);
  border-radius: var(--radius-md);
  animation: level-up-text-in 0.5s ease-out 0.35s both;
}

.level-up-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  animation: level-up-text-in 0.5s ease-out 0.4s both;
}

.level-up-level {
  font-size: 18px;
  font-weight: 700;
  color: #1d4ed8;
}

.level-up-divider {
  color: #93c5fd;
}

.level-up-name {
  font-size: 16px;
  font-weight: 600;
  color: #2563eb;
}

.level-up-exp {
  font-size: 13px;
  color: #3b82f6;
  animation: level-up-text-in 0.5s ease-out 0.45s both;
}

.level-up-preview {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(147, 197, 253, 0.4);
  border-radius: var(--radius-md);
  font-size: 12px;
  color: #1e40af;
  font-weight: 500;
  animation: level-up-text-in 0.5s ease-out 0.55s both;

  .iconfont {
    font-size: 14px;
    color: #3b82f6;
  }
}

.level-up-leave-active {
  transition: all 0.35s ease;
}

.level-up-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px) scale(0.95);
}

@keyframes level-up-in {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(-40px) scale(0.85);
  }
  60% {
    transform: translateX(-50%) translateY(6px) scale(1.02);
  }
  100% {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }
}

@keyframes level-up-glow {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.08);
  }
}

@keyframes level-up-icon-pop {
  0% {
    opacity: 0;
    transform: scale(0.3) rotate(-20deg);
  }
  60% {
    transform: scale(1.15) rotate(5deg);
  }
  100% {
    opacity: 1;
    transform: scale(1) rotate(0);
  }
}

@keyframes level-up-text-in {
  0% {
    opacity: 0;
    transform: translateY(12px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes particle-float {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.5);
  }
  20% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translate(
        calc(-50% + var(--particle-x, 60px)),
        calc(-50% + var(--particle-y, -60px))
    ) scale(1) rotate(45deg);
  }
}

.particle--1 { --particle-x: 70px; --particle-y: -50px; }
.particle--2 { --particle-x: -60px; --particle-y: -70px; }
.particle--3 { --particle-x: 50px; --particle-y: 60px; }
.particle--4 { --particle-x: -70px; --particle-y: 50px; }
.particle--5 { --particle-x: 90px; --particle-y: 10px; }
.particle--6 { --particle-x: -80px; --particle-y: -20px; }
.particle--7 { --particle-x: 30px; --particle-y: -90px; }
.particle--8 { --particle-x: -40px; --particle-y: 80px; }

@media (max-width: 768px) {
  .level-up-notify {
    min-width: 280px;
    max-width: 90%;
  }

  .level-up-content {
    padding: var(--space-5) var(--space-5);
  }

  .level-up-title {
    font-size: 20px;
  }

  .level-up-level {
    font-size: 16px;
  }

  .level-up-name {
    font-size: 14px;
  }
}
</style>
