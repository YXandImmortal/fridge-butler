<template>
  <div class="stat-card" :class="[`stat-card--${type}`]">
    <div class="stat-icon">
      <i class="iconfont" :class="iconClass"/>
    </div>
    <div class="stat-content">
      <div class="stat-value">{{ displayValue }}</div>
      <div class="stat-label">{{ label }}</div>
    </div>
    <div v-if="trend !== null" class="stat-trend" :class="trend > 0 ? 'up' : 'down'">
      {{ trend > 0 ? '↑' : '↓' }} {{ Math.abs(trend) }}%
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
  value: {type: Number, required: true},
  label: {type: String, required: true},
  iconClass: {type: String, default: 'icon-item'},
  type: {type: String, default: 'default'}, // default, warning, danger, success
  trend: {type: Number, default: null},
  suffix: {type: String, default: ''}
})

const displayValue = computed(() => {
  return props.value + props.suffix
})
</script>

<style scoped lang="scss">
.stat-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-5) var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  transition: all 0.3s ease;
  cursor: default;
  position: relative;
  overflow: hidden;

  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: var(--primary-color);
    border-radius: 0 4px 4px 0;
  }
}

.stat-card--warning::before {
  background: var(--warn-color);
}

.stat-card--danger::before {
  background: var(--danger-color);
}

.stat-card--success::before {
  background: var(--success-color);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .iconfont {
    font-size: 24px;
    color: var(--primary-color);
  }
}

.stat-card--warning .stat-icon {
  background: rgba(255, 183, 77, 0.15);

  .iconfont {
    color: var(--warn-color);
  }
}

.stat-card--danger .stat-icon {
  background: var(--danger-light);

  .iconfont {
    color: var(--danger-color);
  }
}

.stat-card--success .stat-icon {
  background: var(--success-light);

  .iconfont {
    color: var(--success-color);
  }
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-trend {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
  background: var(--primary-10);
  color: var(--primary-color);

  &.up {
    background: rgba(129, 212, 250, 0.15);
    color: #4FC3F7;
  }

  &.down {
    background: var(--danger-light);
    color: var(--danger-color);
  }
}

@media (max-width: 768px) {
  .stat-card {
    padding: var(--space-4);
  }

  .stat-value {
    font-size: 22px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;

    .iconfont {
      font-size: 20px;
    }
  }
}
</style>
