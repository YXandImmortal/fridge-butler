<template>
  <div class="freshness-score-card" @click="handleClick">
    <div class="score-main">
      <div class="score-ring" :class="`score-grade--${gradeInfo.label}`">
        <div class="score-value">{{ displayScore }}</div>
        <div v-if="!hasNoData" class="score-grade">{{ gradeInfo.label }}</div>
      </div>
      <div class="score-info">
        <div class="score-title">保鲜评分</div>
        <div class="score-desc">{{ gradeDesc }}</div>
      </div>
    </div>
    <div v-if="showDimensions" class="score-dimensions">
      <div
          v-for="dim in dimensions"
          :key="dim.label"
          class="dimension-item"
      >
        <div class="dimension-header">
          <span class="dimension-label">{{ dim.label }}</span>
          <span class="dimension-value">{{ dim.score }}</span>
        </div>
        <div class="dimension-track">
          <div
              class="dimension-fill"
              :style="{ width: `${Math.min(100, Math.max(0, dim.score))}%` }"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {getScoreGrade} from '@/stores/gamification'

const props = defineProps({
  score: {
    type: Number,
    default: 0
  },
  grade: {
    type: String,
    default: ''
  },
  dimensions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['click'])

const hasNoData = computed(() => props.score === -1 || props.grade === '-')

const displayScore = computed(() => {
  if (hasNoData.value) return '--'
  return Math.round(props.score || 0)
})

const showDimensions = computed(() => {
  return !hasNoData.value && props.dimensions && props.dimensions.length > 0
})

const gradeInfo = computed(() => {
  if (props.grade) {
    const upper = props.grade.toUpperCase()
    const map = {S: 'S', A: 'A', B: 'B', C: 'C', D: 'D'}
    const label = map[upper]
    if (label) {
      return {label}
    }
  }
  return getScoreGrade(props.score) || {label: '-'}
})

const gradeDesc = computed(() => {
  const descMap = {
    S: '状态极佳，继续保持！',
    A: '表现优秀，还有提升空间',
    B: '状态良好，注意临期食材',
    C: '需要关注，建议整理冰箱',
    D: '状态较差，请尽快处理',
    '-': '暂无评分'
  }
  return descMap[gradeInfo.value.label] || ''
})

const handleClick = () => {
  emit('click')
}
</script>

<style scoped lang="scss">
.freshness-score-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  cursor: pointer;
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-2px);
  }
}

.score-main {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.score-ring {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--input-bg);
  border: 3px solid var(--gamification-empty);
  flex-shrink: 0;
  transition: border-color 0.3s ease, background-color 0.3s ease, box-shadow 0.3s ease;
}

.score-ring.score-grade--S {
  border-color: var(--gamification-s);
  background: var(--gamification-s-bg);
  box-shadow: 0 0 16px rgba(34, 197, 94, 0.18);

  .score-value,
  .score-grade {
    color: var(--gamification-s);
  }
}

.score-ring.score-grade--A {
  border-color: var(--gamification-a);
  background: var(--gamification-a-bg);
  box-shadow: 0 0 16px rgba(59, 130, 246, 0.18);

  .score-value,
  .score-grade {
    color: var(--gamification-a);
  }
}

.score-ring.score-grade--B {
  border-color: var(--gamification-b);
  background: var(--gamification-b-bg);
  box-shadow: 0 0 16px rgba(234, 179, 8, 0.18);

  .score-value,
  .score-grade {
    color: var(--gamification-b);
  }
}

.score-ring.score-grade--C {
  border-color: var(--gamification-c);
  background: var(--gamification-c-bg);
  box-shadow: 0 0 16px rgba(249, 115, 22, 0.18);

  .score-value,
  .score-grade {
    color: var(--gamification-c);
  }
}

.score-ring.score-grade--D {
  border-color: var(--gamification-d);
  background: var(--gamification-d-bg);
  box-shadow: 0 0 16px rgba(239, 68, 68, 0.18);

  .score-value,
  .score-grade {
    color: var(--gamification-d);
  }
}

.score-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.1;
}

.score-grade {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
}

.score-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.score-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.score-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.score-dimensions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3);
}

.dimension-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.dimension-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  line-height: 1.2;
}

.dimension-label {
  color: var(--text-secondary);
}

.dimension-value {
  color: var(--text-primary);
  font-weight: 600;
}

.dimension-track {
  height: 4px;
  background: var(--input-bg);
  border-radius: 9999px;
  overflow: hidden;
}

.dimension-fill {
  height: 100%;
  background: var(--gradient-btn);
  border-radius: 9999px;
  transition: width 0.6s ease;
}

@media (max-width: 480px) {
  .score-dimensions {
    grid-template-columns: 1fr;
  }
}
</style>
