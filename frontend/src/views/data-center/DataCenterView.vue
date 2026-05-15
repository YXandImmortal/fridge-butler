<template>
  <div class="data-center-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">数据中心</h1>
      <p class="page-subtitle">冰箱与食材的数据洞察</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="8" animated />
    </div>

    <template v-else>
      <!-- 顶部概览指标 -->
      <div class="stats-row">
        <StatCard
          :value="overviewStats.fridgeCount"
          label="冰箱数量"
          icon-class="icon-fridge-line"
          type="default"
          suffix=" 台"
        />
        <StatCard
          :value="overviewStats.totalItems"
          label="物品总件数"
          icon-class="icon-inbox-full"
          type="success"
          suffix=" 件"
        />
        <StatCard
          :value="overviewStats.expiringCount"
          label="临期 / 过期预警"
          icon-class="icon-warning"
          type="warning"
          suffix=" 件"
        />
        <StatCard
          :value="overviewStats.capacityRate"
          label="平均容量利用率"
          icon-class="icon-chart-pie"
          type="default"
          suffix="%"
        />
      </div>

      <!-- 第一行图表：冰箱分布 + 分类占比 -->
      <div class="charts-row">
        <div class="chart-col">
          <FridgeItemBarChart :data="fridgeBarData" />
        </div>
        <div class="chart-col">
          <CategoryPieChart :data="categoryPieData" />
        </div>
      </div>

      <!-- 第二行图表：新鲜度 + 入库趋势 -->
      <div class="charts-row">
        <div class="chart-col">
          <FreshnessPieChart :data="freshnessPieData" />
        </div>
        <div class="chart-col">
          <TrendLineChart :data="trendData" />
        </div>
      </div>

      <!-- 第三行图表：保质期分布 + 容量仪表盘 -->
      <div class="charts-row">
        <div class="chart-col">
          <ShelfLifeBarChart :data="shelfLifeBarData" />
        </div>
        <div class="chart-col">
          <CapacityGaugeGroup :data="fridgeBarData" />
        </div>
      </div>

      <!-- 底部预警清单 -->
      <div class="table-row">
        <ExpiringItemTable :data="expiringItems" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listMyFridges } from '@/api/fridge'
import { searchItems } from '@/api/item'
import showMessage from '@/utils/message'
import StatCard from '@/components/data-center/StatCard.vue'
import FridgeItemBarChart from '@/components/data-center/FridgeItemBarChart.vue'
import CategoryPieChart from '@/components/data-center/CategoryPieChart.vue'
import FreshnessPieChart from '@/components/data-center/FreshnessPieChart.vue'
import TrendLineChart from '@/components/data-center/TrendLineChart.vue'
import ShelfLifeBarChart from '@/components/data-center/ShelfLifeBarChart.vue'
import CapacityGaugeGroup from '@/components/data-center/CapacityGaugeGroup.vue'
import ExpiringItemTable from '@/components/data-center/ExpiringItemTable.vue'
import {
  computeOverviewStats,
  aggregateByFridge,
  aggregateByCategory,
  aggregateFreshness,
  computeInboundTrend,
  aggregateShelfLifeDistribution,
  getExpiringItems
} from '@/utils/data-analysis'

// ==================== 数据状态 ====================
const loading = ref(false)
const fridgeList = ref([])
const itemList = ref([])

// ==================== 计算属性：聚合数据 ====================
const overviewStats = computed(() => computeOverviewStats(fridgeList.value, itemList.value))
const fridgeBarData = computed(() => aggregateByFridge(fridgeList.value))
const categoryPieData = computed(() => aggregateByCategory(itemList.value))
const freshnessPieData = computed(() => aggregateFreshness(itemList.value))
const trendData = computed(() => computeInboundTrend(itemList.value))
const shelfLifeBarData = computed(() => aggregateShelfLifeDistribution(itemList.value))
const expiringItems = computed(() => getExpiringItems(itemList.value, fridgeList.value))

// ==================== 数据获取 ====================
const fetchData = async () => {
  loading.value = true
  try {
    const [fridgeRes, itemRes] = await Promise.all([
      listMyFridges(),
      searchItems({})
    ])

    if (fridgeRes.code === 200 && Array.isArray(fridgeRes.data)) {
      fridgeList.value = fridgeRes.data
    } else {
      fridgeList.value = []
    }

    if (itemRes.code === 200 && Array.isArray(itemRes.data)) {
      itemList.value = itemRes.data
    } else {
      itemList.value = []
    }
  } catch (error) {
    console.error('获取数据中心数据失败:', error)
    showMessage.error('获取数据失败')
    fridgeList.value = []
    itemList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.data-center-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
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
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-6);
  margin-bottom: var(--space-6);
}

.chart-col {
  min-width: 0;
}

/* 底部表格行 */
.table-row {
  margin-bottom: var(--space-6);
}

/* 响应式适配 */
@media (max-width: 1100px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
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

  .charts-row {
    grid-template-columns: 1fr;
    gap: var(--space-4);
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
