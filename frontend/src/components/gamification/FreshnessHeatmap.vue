<template>
  <div class="freshness-heatmap">
    <div class="heatmap-scroll-wrapper">
      <!-- 左侧星期标签，滚动时固定 -->
      <div class="heatmap-weekdays">
        <span v-for="day in weekdayLabels" :key="day" class="weekday-label">{{ day }}</span>
      </div>

      <!-- 右侧月份 + 网格，可横向滚动 -->
      <div class="heatmap-content">
        <div class="heatmap-header">
          <div class="heatmap-months">
            <span
                v-for="month in monthLabels"
                :key="month.label + month.offset"
                class="month-label"
                :style="{ left: month.offset + 'px' }"
            >
              {{ month.label }}
            </span>
          </div>
        </div>

        <div class="heatmap-grid" :style="gridStyle">
          <el-tooltip
              v-for="cell in cells"
              :key="cell.date || cell.key"
              placement="top"
              :show-after="200"
          >
            <template #content>
              <div class="heatmap-tooltip">
                <div class="tooltip-date">{{ cell.date || '-' }}</div>
                <div class="tooltip-score">
                  <span v-if="cell.hasData">评分 {{ cell.score }} · 等级 {{ cell.grade }}</span>
                  <span v-else>暂无数据</span>
                </div>
                <div v-if="cell.hasExpired" class="tooltip-expired">当日有过期食材</div>
              </div>
            </template>
            <div
                class="heatmap-cell"
                :class="[`grade--${cell.gradeClass}`, { 'is-empty': !cell.hasData }]"
            />
          </el-tooltip>
        </div>
      </div>
    </div>

    <div class="heatmap-footer">
      <span class="legend-label">等级图例</span>
      <div class="legend-items">
        <div class="legend-item">
          <div class="legend-dot grade--empty"/>
          <span>无数据</span>
        </div>
        <div class="legend-item">
          <div class="legend-dot grade--D"/>
          <span>D</span>
        </div>
        <div class="legend-item">
          <div class="legend-dot grade--C"/>
          <span>C</span>
        </div>
        <div class="legend-item">
          <div class="legend-dot grade--B"/>
          <span>B</span>
        </div>
        <div class="legend-item">
          <div class="legend-dot grade--A"/>
          <span>A</span>
        </div>
        <div class="legend-item">
          <div class="legend-dot grade--S"/>
          <span>S</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const CELL_SIZE = 14
const CELL_GAP = 3
const WEEKDAY_LABELS = ['一', '二', '三', '四', '五', '六', '日']

const sortedData = computed(() => {
  return [...props.data].sort((a, b) => new Date(a.date) - new Date(b.date))
})

const cells = computed(() => {
  if (sortedData.value.length === 0) return []

  const first = sortedData.value[0]
  const firstDate = new Date(first.date.replace(/-/g, '/'))
  const startDay = firstDate.getDay() || 7 // 周日=7
  const offset = startDay - 1 // 周一对齐需要补的空白天数

  const emptyCells = Array.from({length: offset}, (_, i) => ({
    key: `padding-${i}`,
    date: '',
    score: -1,
    grade: '-',
    gradeClass: 'empty',
    hasData: false,
    hasExpired: false,
    isPadding: true
  }))

  const dataCells = sortedData.value.map((item) => {
    const hasData = item.score >= 0 && item.grade && item.grade !== '-'
    return {
      key: item.date,
      date: item.date,
      score: item.score,
      grade: item.grade,
      gradeClass: hasData ? item.grade.toUpperCase() : 'empty',
      hasData,
      hasExpired: !!item.hasExpired
    }
  })

  return [...emptyCells, ...dataCells]
})

const totalCols = computed(() => {
  return Math.ceil(cells.value.length / 7)
})

const gridStyle = computed(() => {
  if (totalCols.value === 0) return {}
  const width = totalCols.value * CELL_SIZE + (totalCols.value - 1) * CELL_GAP
  return {
    width: `${width}px`,
    gridTemplateColumns: `repeat(${totalCols.value}, ${CELL_SIZE}px)`,
    gridTemplateRows: `repeat(7, ${CELL_SIZE}px)`
  }
})

const monthLabels = computed(() => {
  if (sortedData.value.length === 0) return []
  const labels = []
  let lastMonth = ''

  sortedData.value.forEach((item) => {
    const date = new Date(item.date.replace(/-/g, '/'))
    const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
    if (monthKey !== lastMonth) {
      labels.push({
        label: `${date.getMonth() + 1}月`,
        date: item.date
      })
      lastMonth = monthKey
    }
  })

  const paddingOffset = cells.value.findIndex((cell) => !cell.isPadding)

  return labels.map((label) => {
    const dataIndex = sortedData.value.findIndex((item) => item.date === label.date)
    const cellIndex = paddingOffset + dataIndex
    const weekIndex = Math.floor(cellIndex / 7)
    return {
      label: label.label,
      offset: weekIndex * (CELL_SIZE + CELL_GAP)
    }
  })
})

const weekdayLabels = computed(() => WEEKDAY_LABELS)
</script>

<style scoped lang="scss">
.freshness-heatmap {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.heatmap-scroll-wrapper {
  display: flex;
  gap: var(--space-2);
  overflow-x: auto;
  padding-bottom: var(--space-2);
  max-width: 100%;
  min-width: 0;

  &::-webkit-scrollbar {
    height: 6px;
  }

  &::-webkit-scrollbar-track {
    background: var(--input-bg);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--border-color);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: var(--text-tertiary);
  }
}

.heatmap-weekdays {
  position: sticky;
  left: 0;
  z-index: 1;
  display: grid;
  grid-template-rows: repeat(7, 14px);
  gap: 3px;
  padding-top: 22px; // 18px 月份头 + 4px 内容间距
  background: var(--card-bg);
}

.weekday-label {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 10px;
  color: var(--text-tertiary);
  line-height: 1;
  text-align: right;
}

.heatmap-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.heatmap-header {
  position: relative;
  height: 18px;
}

.heatmap-months {
  position: relative;
  width: 100%;
  height: 100%;
}

.month-label {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 11px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.heatmap-grid {
  display: grid;
  grid-auto-flow: column;
  gap: 3px;
}

.heatmap-cell {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  background: var(--gamification-empty);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;

  &:hover {
    transform: scale(1.15);
    box-shadow: 0 0 0 2px var(--primary-30);
  }

  &.grade--S { background: var(--gamification-s); }
  &.grade--A { background: var(--gamification-a); }
  &.grade--B { background: var(--gamification-b); }
  &.grade--C { background: var(--gamification-c); }
  &.grade--D { background: var(--gamification-d); }
  &.is-empty { background: var(--input-bg); opacity: 0.7; }
}

.heatmap-tooltip {
  text-align: center;
  line-height: 1.5;

  .tooltip-date {
    font-size: 12px;
    font-weight: 600;
    margin-bottom: 2px;
  }

  .tooltip-score {
    font-size: 11px;
  }

  .tooltip-expired {
    font-size: 11px;
    color: var(--gamification-d);
    margin-top: 2px;
  }
}

.heatmap-footer {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.legend-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.legend-items {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--text-secondary);
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;

  &.grade--S { background: var(--gamification-s); }
  &.grade--A { background: var(--gamification-a); }
  &.grade--B { background: var(--gamification-b); }
  &.grade--C { background: var(--gamification-c); }
  &.grade--D { background: var(--gamification-d); }
  &.grade--empty { background: var(--input-bg); opacity: 0.7; }
}

@media (max-width: 768px) {
  .heatmap-weekdays {
    grid-template-rows: repeat(7, 12px);
    gap: 2px;
  }

  .heatmap-cell {
    width: 12px;
    height: 12px;
  }

  .heatmap-grid {
    gap: 2px;
  }
}
</style>
