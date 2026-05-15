<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <i class="iconfont icon-item chart-title-icon" />
        <h3 class="chart-title">新鲜度状态分布</h3>
      </div>
    </div>
    <v-chart
      v-if="data.length > 0"
      class="chart-body"
      :option="chartOption"
      autoresize
    />
    <el-empty v-else description="暂无数据" class="chart-empty" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { getChartThemeColors } from '@/utils/data-analysis'
import { useThemeStore } from '@/stores/theme'

use([CanvasRenderer, PieChart, TooltipComponent, LegendComponent])

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const themeStore = useThemeStore()

const statusColors = {
  '新鲜': '#81C784',
  '一般': '#64B5F6',
  '临期': '#FFB74D',
  '已过期': '#F87171'
}

const chartOption = computed(() => {
  const colors = getChartThemeColors(themeStore.theme === 'dark')
  const mappedData = props.data.map(d => ({
    ...d,
    itemStyle: { color: statusColors[d.name] || colors.colors[0] }
  }))

  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.textColor },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: { color: colors.subTextColor, fontSize: 12 },
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 20
    },
    series: [{
      name: '新鲜度',
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: colors.tooltipBg,
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{c}',
        color: colors.subTextColor,
        fontSize: 12
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: colors.textColor
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      data: mappedData
    }]
  }
})
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
  min-height: 320px;
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

.chart-body {
  flex: 1;
  min-height: 260px;
}

.chart-empty {
  flex: 1;
  min-height: 260px;
}

@media (max-width: 768px) {
  .chart-card {
    padding: var(--space-4);
  }
}
</style>
