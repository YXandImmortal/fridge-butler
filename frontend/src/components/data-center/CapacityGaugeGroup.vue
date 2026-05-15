<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <i class="iconfont icon-fridge-line chart-title-icon" />
        <h3 class="chart-title">冰箱容量利用率</h3>
      </div>
    </div>
    <div v-if="data.length > 0" class="gauge-grid">
      <div v-for="(item, index) in data" :key="index" class="gauge-item">
        <v-chart
          class="gauge-chart"
          :option="getGaugeOption(item, index)"
          autoresize
        />
        <div class="gauge-name">{{ item.name }}</div>
      </div>
    </div>
    <el-empty v-else description="暂无冰箱数据" class="chart-empty" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GaugeChart } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { getChartThemeColors } from '@/utils/data-analysis'
import { useThemeStore } from '@/stores/theme'

use([CanvasRenderer, GaugeChart, TooltipComponent])

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const themeStore = useThemeStore()

function getGaugeColor(rate) {
  if (rate < 50) return '#81C784'
  if (rate < 80) return '#FFB74D'
  return '#F87171'
}

function getGaugeOption(item, index) {
  const colors = getChartThemeColors(themeStore.theme === 'dark')
  const rate = item.totalCapacity > 0
    ? Math.round((item.value / item.totalCapacity) * 100)
    : 0
  const color = getGaugeColor(rate)

  return {
    series: [{
      type: 'gauge',
      startAngle: 200,
      endAngle: -20,
      min: 0,
      max: 100,
      radius: '90%',
      center: ['50%', '55%'],
      splitNumber: 5,
      itemStyle: {
        color: color
      },
      progress: {
        show: true,
        width: 12,
        roundCap: true
      },
      pointer: {
        show: false
      },
      axisLine: {
        lineStyle: {
          width: 12,
          color: [[1, colors.splitLineColor]]
        }
      },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      title: { show: false },
      detail: {
        valueAnimation: true,
        fontSize: 20,
        fontWeight: 'bold',
        color: colors.textColor,
        offsetCenter: [0, '0%'],
        formatter: '{value}%'
      },
      data: [{ value: rate }]
    }]
  }
}
</script>

<style scoped lang="scss">
.chart-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  max-height: 370px;
  height: 100%;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--gray-40);
}

.chart-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-title-icon {
  font-size: 22px;
  color: var(--primary-color);
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.gauge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: var(--space-4);
  flex: 1;
  align-items: start;
}

.gauge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.gauge-chart {
  width: 100%;
  height: 140px;
}

.gauge-name {
  font-size: 13px;
  color: var(--text-secondary);
  text-align: center;
  margin-top: -10px;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 var(--space-2);
}

.chart-empty {
  flex: 1;
  min-height: 260px;
}

@media (max-width: 768px) {
  .chart-card {
    padding: var(--space-4);
  }

  .gauge-chart {
    height: 120px;
  }
}
</style>
