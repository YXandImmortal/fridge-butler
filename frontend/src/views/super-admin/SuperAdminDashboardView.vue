<template>
  <div class="admin-dashboard-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <i class="iconfont icon-chart page-header-icon"/>
        <h1 class="page-title">数据看板</h1>
      </div>
      <p class="page-subtitle">系统运营数据总览</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="8" animated />
      <p class="loading-hint">数据加载中，请稍候...</p>
    </div>

    <template v-else>
      <!-- 顶部统计卡片 -->
      <div class="stats-row">
        <StatCard
          :value="stats.totalUsers"
          label="总用户数"
          icon-class="icon-users"
          suffix=" 人"
        />
        <StatCard
          :value="stats.todayNewUsers"
          label="今日新增用户"
          icon-class="icon-user-plus"
          type="success"
          suffix=" 人"
        />
        <StatCard
          :value="stats.totalFridges"
          label="冰箱总数"
          icon-class="icon-fridge-line"
          suffix=" 台"
        />
        <StatCard
          :value="stats.totalItems"
          label="物品总数"
          icon-class="icon-item"
          suffix=" 件"
        />
      </div>

      <!-- 图表行 -->
      <div class="charts-row">
        <div class="chart-col chart-col--wide">
          <div class="chart-card">
            <div class="chart-header">
              <div class="chart-title-wrapper">
                <i class="iconfont icon-trending chart-title-icon" />
                <h3 class="chart-title">近7天用户注册趋势</h3>
              </div>
            </div>
            <v-chart
              v-if="hasTrendData"
              class="chart-body"
              :option="trendChartOption"
              autoresize
            />
            <el-empty v-else description="暂无注册数据" class="chart-empty" />
          </div>
        </div>
        <div class="chart-col">
          <div class="chart-card">
            <div class="chart-header">
              <div class="chart-title-wrapper">
                <i class="iconfont icon-charts-pie chart-title-icon" />
                <h3 class="chart-title">冰箱类型分布</h3>
              </div>
            </div>
            <v-chart
              v-if="hasFridgeTypeData"
              class="chart-body"
              :option="pieChartOption"
              autoresize
            />
            <el-empty v-else description="暂无数据" class="chart-empty" />
          </div>
        </div>
      </div>

      <!-- 底部日志表格 -->
      <div class="table-row">
        <div class="chart-card">
          <div class="chart-header">
            <div class="chart-title-wrapper">
              <i class="iconfont icon-script-text chart-title-icon" />
              <h3 class="chart-title">最近操作日志</h3>
            </div>
          </div>
          <el-table :data="recentLogs" style="width: 100%">
            <el-table-column prop="username" label="用户" min-width="120" />
            <el-table-column prop="method" label="方法" width="90">
              <template #default="{ row }">
                <el-tag :type="methodTagType(row.method)" size="small">{{ row.method }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="uri" label="请求路径" min-width="180" />
            <el-table-column prop="statusCode" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.statusCode)" size="small">{{ row.statusCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="durationMs" label="耗时" width="90">
              <template #default="{ row }">
                {{ row.durationMs }}ms
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" min-width="160" />
          </el-table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use, graphic } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { LegacyGridContainLabel } from 'echarts/features'
import VChart from 'vue-echarts'
import StatCard from '@/components/data-center/StatCard.vue'
import { getDashboardStats, getDashboardTrend, getLogList } from '@/api/admin.js'
import { getChartThemeColors } from '@/utils/data-analysis.js'
import { useThemeStore } from '@/stores/theme.js'
import showMessage from '@/utils/message.js'

use([CanvasRenderer, PieChart, LineChart, GridComponent, TooltipComponent, LegendComponent, LegacyGridContainLabel])

// ==================== 数据状态 ====================
const loading = ref(false)
const stats = ref({
  totalUsers: 0,
  todayNewUsers: 0,
  totalFridges: 0,
  totalItems: 0
})
const trendRawData = ref([])
const fridgeTypeData = ref([])
const recentLogs = ref([])

const themeStore = useThemeStore()

// ==================== 计算属性 ====================

function hexToRgba(hex, alpha) {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r},${g},${b},${alpha})`
}

// 折线图是否有数据（只要后端返回了日期数组就认为有数据，允许全为0）
const hasTrendData = computed(() => {
  return trendRawData.value.length > 0
})

// 折线图配置
const trendChartOption = computed(() => {
  const colors = getChartThemeColors(themeStore.theme === 'dark')
  const dates = trendRawData.value.map(item => item.date)
  const counts = trendRawData.value.map(item => item.newUsers ?? 0)

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
      data: ['新增用户'],
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
      data: dates,
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
    series: [
      {
        name: '新增用户',
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
            { offset: 0, color: hexToRgba('#64B5F6', 0.35) },
            { offset: 1, color: hexToRgba('#64B5F6', 0.02) }
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
        data: counts
      }
    ]
  }
})

// 饼图是否有数据
const hasFridgeTypeData = computed(() => fridgeTypeData.value.length > 0)

// 饼图配置
const pieChartOption = computed(() => {
  const colors = getChartThemeColors(themeStore.theme === 'dark')

  return {
    color: colors.colors,
    tooltip: {
      trigger: 'item',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.textColor },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: colors.subTextColor, fontSize: 12 },
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 14
    },
    series: [
      {
        name: '冰箱类型',
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: colors.tooltipBg,
          borderWidth: 2
        },
        label: {
          show: false
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
        labelLine: {
          show: false
        },
        data: fridgeTypeData.value
      }
    ]
  }
})

// ==================== 辅助函数 ====================

const methodTagType = (method) => {
  const map = {
    GET: 'primary',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return map[method] || 'info'
}

const statusTagType = (code) => {
  if (code >= 200 && code < 300) return 'success'
  if (code >= 400 && code < 500) return 'warning'
  if (code >= 500) return 'danger'
  return 'info'
}

// ==================== 数据获取 ====================
const fetchData = async () => {
  loading.value = true
  try {
    const [statsRes, trendRes, logsRes] = await Promise.all([
      getDashboardStats().catch(err => {
        console.error('获取统计数据失败:', err)
        return { code: -1, data: null }
      }),
      getDashboardTrend(7).catch(err => {
        console.error('获取趋势数据失败:', err)
        return { code: -1, data: [] }
      }),
      getLogList({ page: 1, size: 10 }).catch(err => {
        console.error('获取日志数据失败:', err)
        return { code: -1, data: { list: [] } }
      })
    ])

    // 统计数据（兼容后端实际字段名）
    if (statsRes.code === 200 && statsRes.data) {
      const d = statsRes.data
      stats.value = {
        totalUsers: d.userTotal ?? 0,
        todayNewUsers: d.userToday ?? 0,
        totalFridges: d.fridgeTotal ?? 0,
        totalItems: d.itemTotal ?? 0
      }
      fridgeTypeData.value = d.fridgeTypeDistribution || []
    }

    // 趋势数据
    if (trendRes.code === 200 && Array.isArray(trendRes.data)) {
      trendRawData.value = trendRes.data
    } else {
      trendRawData.value = []
    }

    // 日志数据
    if (logsRes.code === 200 && logsRes.data) {
      const list = logsRes.data.list || []
      recentLogs.value = list.slice(0, 10)
    } else {
      recentLogs.value = []
    }
  } catch (error) {
    console.error('获取看板数据失败:', error)
    showMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.admin-dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin: 0 0 var(--space-2) 0;
}

.page-header-icon {
  font-size: 28px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.loading-wrapper {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  box-shadow: var(--shadow-sm);
}

.loading-hint {
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: var(--space-4);
}

/* 顶部指标行 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
  margin-bottom: var(--space-6);
}

/* 图表行 */
.charts-row {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
}

.chart-col {
  min-width: 0;
}

/* 图表卡片 —— 与 data-center 的 chart-card 保持一致 */
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

/* 表格行 */
.table-row {
  margin-bottom: var(--space-6);
}

/* 响应式适配 */
@media (max-width: 1100px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-4);
  }

  .chart-card {
    padding: var(--space-4);
    min-height: 280px;
  }
}

@media (max-width: 480px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 20px;
  }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
