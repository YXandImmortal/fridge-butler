<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <i class="iconfont icon-fridge-line chart-title-icon"/>
        <h3 class="chart-title">各冰箱物品数量</h3>
      </div>
    </div>
    <v-chart
        v-if="data.length > 0"
        class="chart-body"
        :option="chartOption"
        autoresize
    />
    <el-empty v-else description="暂无数据" class="chart-empty"/>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {use, graphic} from 'echarts/core'
import {CanvasRenderer} from 'echarts/renderers'
import {BarChart} from 'echarts/charts'
import {GridComponent, TooltipComponent, LegendComponent} from 'echarts/components'
import {LegacyGridContainLabel} from 'echarts/features'
import VChart from 'vue-echarts'
import {getChartThemeColors} from '@/utils/data-analysis'
import {useThemeStore} from '@/stores/theme'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent, LegacyGridContainLabel])

const props = defineProps({
  data: {type: Array, default: () => []}
})

const themeStore = useThemeStore()

const chartOption = computed(() => {
  const colors = getChartThemeColors(themeStore.theme === 'dark')
  const names = props.data.map(d => d.name)
  const values = props.data.map(d => d.value)

  return {
    color: colors.colors,
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: {color: colors.textColor},
      axisPointer: {type: 'shadow'}
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLine: {lineStyle: {color: colors.axisLineColor}},
      axisLabel: {color: colors.subTextColor, interval: 0, rotate: names.length > 5 ? 30 : 0}
    },
    yAxis: {
      type: 'value',
      axisLine: {show: false},
      splitLine: {lineStyle: {color: colors.splitLineColor, type: 'dashed'}},
      axisLabel: {color: colors.subTextColor}
    },
    series: [{
      name: '物品数量',
      type: 'bar',
      barWidth: '50%',
      data: values,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new graphic.LinearGradient(0, 0, 0, 1, [
          {offset: 0, color: '#64B5F6'},
          {offset: 1, color: 'rgba(100,181,246,0.3)'}
        ])
      },
      emphasis: {
        itemStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            {offset: 0, color: '#42A5F5'},
            {offset: 1, color: 'rgba(66,165,245,0.4)'}
          ])
        }
      }
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
</style>
