<template>
  <div class="streak-card" :class="{ 'is-melt-warning': meltWarning }">
    <div class="streak-main">
      <div class="streak-ice" :class="{ 'is-melting': meltWarning }">
        <span class="ice-icon">🧊</span>
        <span class="streak-count">× {{ currentStreak }}</span>
      </div>
      <div class="streak-label">冰鲜连续天数</div>
    </div>
    <div class="streak-meta">
      <div class="meta-item">
        <span class="meta-value">{{ maxStreak }}</span>
        <span class="meta-label">历史最高</span>
      </div>
      <div class="meta-item">
        <span class="meta-value">{{ protectRemaining }}/{{ protectTotal }}</span>
        <span class="meta-label">本月保护</span>
      </div>
      <div v-if="protectAutoEnabled" class="meta-item">
        <span class="meta-value auto">自动</span>
        <span class="meta-label">保护开关</span>
      </div>
    </div>
    <div v-if="meltWarning" class="melt-warning">
      <i class="iconfont icon-alert"/>
      <span>今日有过期食材，冰鲜即将融化</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  currentStreak: {
    type: Number,
    default: 0
  },
  maxStreak: {
    type: Number,
    default: 0
  },
  protectRemaining: {
    type: Number,
    default: 0
  },
  protectTotal: {
    type: Number,
    default: 0
  },
  protectAutoEnabled: {
    type: Boolean,
    default: false
  },
  meltWarning: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped lang="scss">
.streak-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  height: 100%;
  justify-content: center;
}

.streak-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.streak-ice {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.ice-icon {
  font-size: 32px;
  line-height: 1;
}

.streak-count {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.streak-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.streak-ice.is-melting .ice-icon {
  animation: melt-warning 1.2s ease-in-out infinite;
}

.streak-meta {
  display: flex;
  gap: var(--space-4);
  padding-top: var(--space-2);
  border-top: 1px solid var(--border-color);
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.meta-value {
  display: flex;
  align-items: flex-end;
  min-height: 22px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.meta-value.auto {
  color: var(--success-color);
}

.meta-label {
  font-size: 11px;
  color: var(--text-tertiary);
}

.melt-warning {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--danger-light);
  border-radius: var(--radius-sm);
  color: var(--danger-color);
  font-size: 12px;
  font-weight: 500;

  .iconfont {
    font-size: 14px;
  }
}
</style>
