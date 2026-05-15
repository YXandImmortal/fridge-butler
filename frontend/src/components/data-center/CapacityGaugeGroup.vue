<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <i class="iconfont icon-fridge-line chart-title-icon" />
        <h3 class="chart-title">冰箱容量利用率</h3>
      </div>
    </div>
    <el-carousel
      v-if="data.length > 0"
      class="gauge-carousel"
      :interval="10000"
      arrow="hover"
      indicator-position="outside"
      height="280px"
      @change="handleChange"
    >
      <el-carousel-item v-for="(group, gIndex) in groupedData" :key="gIndex">
        <div class="gauge-slide">
          <div v-for="(item, index) in group" :key="index" class="gauge-item">
            <v-chart
              v-if="gIndex === activeIndex"
              class="gauge-chart"
              :option="getGaugeOption(item)"
              :init-options="chartInitOptions"
              autoresize
            />
            <div v-else class="gauge-chart gauge-chart-placeholder" />
            <div class="gauge-name">{{ item.name }}</div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
    <el-empty v-else description="暂无冰箱数据" class="chart-empty" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
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
const activeIndex = ref(0)
const chartInitOptions = { width: 160, height: 160 }

function handleChange(index) {
  activeIndex.value = index
}

const groupedData = computed(() => {
  const groups = []
  for (let i = 0; i < props.data.length; i += 2) {
    groups.push(props.data.slice(i, i + 2))
  }
  return groups
})

function getGaugeColor(rate) {
  if (rate < 50) return '#81C784'
  if (rate < 80) return '#FFB74D'
  return '#F87171'
}

function getGaugeOption(item) {
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

.gauge-carousel {
  flex: 1;
  width: 100%;
}

.gauge-slide {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-10);
  height: 100%;
  padding: var(--space-2) var(--space-8);
}

.gauge-item {
  width: 180px;
  flex-shrink: 0;
}

.gauge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.gauge-chart {
  width: 160px;
  height: 160px;
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

  .gauge-slide {
    gap: var(--space-4);
    padding: var(--space-2) var(--space-4);
  }

  .gauge-item {
    width: 130px;
  }

  .gauge-chart {
    width: 130px;
    height: 130px;
  }
}
</style>
