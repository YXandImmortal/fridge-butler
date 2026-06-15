<template>
  <div class="exp-progress-bar">
    <div class="exp-header">
      <span class="exp-label">EXP</span>
      <span class="exp-value">{{ currentExp }} / {{ requiredExp }}</span>
    </div>
    <div class="exp-track">
      <div class="exp-fill" :style="{ width: `${progress}%` }">
        <div class="exp-shimmer"/>
      </div>
    </div>
    <div class="exp-footer">
      <span class="exp-today">
        今日 +{{ todayExp }} / {{ todayExpLimit }} EXP
      </span>
      <span v-if="isTodayFull" class="exp-today-full">已达上限</span>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
  currentExp: {
    type: Number,
    default: 0
  },
  requiredExp: {
    type: Number,
    default: 100
  },
  todayExp: {
    type: Number,
    default: 0
  },
  todayExpLimit: {
    type: Number,
    default: 150
  }
})

const progress = computed(() => {
  if (!props.requiredExp || props.requiredExp <= 0) return 0
  return Math.min(100, Math.max(0, (props.currentExp / props.requiredExp) * 100))
})

const isTodayFull = computed(() => props.todayExp >= props.todayExpLimit)
</script>

<style scoped lang="scss">
.exp-progress-bar {
  width: 100%;
}

.exp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.exp-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.exp-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.exp-track {
  width: 100%;
  height: 10px;
  background: var(--input-bg);
  border-radius: 9999px;
  overflow: hidden;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.06);
}

.exp-fill {
  height: 100%;
  background: var(--gradient-btn);
  border-radius: 9999px;
  transition: width 0.6s ease;
  position: relative;
  overflow: hidden;
}

.exp-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.35) 50%,
      transparent 100%
  );
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
}

.exp-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-2);
}

.exp-today {
  font-size: 12px;
  color: var(--text-secondary);
}

.exp-today-full {
  font-size: 11px;
  font-weight: 600;
  color: var(--success-color);
  padding: 2px 8px;
  background: var(--success-light);
  border-radius: 9999px;
}
</style>
