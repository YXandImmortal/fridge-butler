<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <i class="iconfont icon-trending chart-title-icon" />
        <h3 class="chart-title">{{ title }}</h3>
      </div>
    </div>
    <v-chart
      v-if="hasData"
      class="chart-body"
      :option="chartOption"
      autoresize
    />
    <el-empty v-else :description="emptyText" class="chart-empty" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { use, graphic } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { LegacyGridContainLabel } from 'echarts/features'
import VChart from 'vue-echarts'
import { getChartThemeColors } from '@/utils/data-analysis'
import { useThemeStore } from '@/stores/theme'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent, LegacyGridContainLabel])

const props = defineProps({
  title: {
    type: String,
    default: '近30天入库趋势'
  },
  emptyText: {
    type: String,
    default: '近30天暂无入库记录'
  },
  data: {
    type: Object,
    default: () => ({ dates: [], counts: [] })
  }
})

const themeStore = useThemeStore()

function hexToRgba(hex, alpha) {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r},${g},${b},${alpha})`
}

const hasData = computed(() => {
  if (!props.data.dates || props.data.dates.length === 0) return false
  if (props.data.series && Array.isArray(props.data.series)) {
    return props.data.series.some(s => Array.isArray(s.counts) && s.counts.some(c => c > 0))
  }
  return Array.isArray(props.data.counts) && props.data.counts.some(c => c > 0)
})

const chartOption = computed(() => {
  const colors = getChartThemeColors(themeStore.theme === 'dark')

  const isMultiSeries = props.data.series && Array.isArray(props.data.series)

  const seriesList = isMultiSeries
    ? props.data.series.map((s, index) => {
        const color = s.color || colors.colors[index % colors.colors.length]
        return {
          name: s.name,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          showSymbol: false,
          lineStyle: {
            width: 3,
            color
          },
          areaStyle: {
            color: new graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: hexToRgba(color, 0.35) },
              { offset: 1, color: hexToRgba(color, 0.02) }
            ])
          },
          itemStyle: {
            color,
            borderColor: colors.tooltipBg,
            borderWidth: 2
          },
          emphasis: {
            focus: 'series',
            itemStyle: { borderWidth: 3 }
          },
          data: s.counts || []
        }
      })
    : [{
        name: '入库数量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        lineStyle: {
          width: 3,
          color: '#64B5F6'
        },
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(100,181,246,0.35)' },
            { offset: 1, color: 'rgba(100,181,246,0.02)' }
          ])
        },
        itemStyle: {
          color: '#64B5F6',
          borderColor: colors.tooltipBg,
          borderWidth: 2
        },
        emphasis: {
          focus: 'series',
          itemStyle: { borderWidth: 3 }
        },
        data: props.data.counts || []
      }]

  return {
    color: colors.colors,
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.textColor },
      axisPointer: {
        type: 'line',
        lineStyle: { color: colors.primaryColor, width: 1, type: 'dashed' }
      }
    },
    legend: {
      data: seriesList.map(s => s.name),
      top: '2%',
      textStyle: { color: colors.subTextColor, fontSize: 12 },
      itemWidth: 12,
      itemHeight: 8
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '18%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.data.dates,
      axisLine: { lineStyle: { color: colors.axisLineColor } },
      axisLabel: {
        color: colors.subTextColor,
        fontSize: 11,
        formatter: (value) => {
          if (typeof value === 'string' && value.includes('-')) {
            const d = new Date(value)
            return `${d.getMonth() + 1}/${d.getDate()}`
          }
          return value
        }
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: colors.splitLineColor, type: 'dashed' } },
      axisLabel: { color: colors.subTextColor }
    },
    series: seriesList
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
