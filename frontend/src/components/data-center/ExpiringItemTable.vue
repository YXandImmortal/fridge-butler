<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <i class="iconfont icon-alert chart-title-icon" />
        <h3 class="chart-title">临期 / 过期物品预警</h3>
      </div>
      <el-tag
        v-if="data.length > 0"
        :type="data.some(d => d.freshnessLabel === '已过期') ? 'danger' : 'warning'"
        size="small"
        effect="light"
      >
        共 {{ data.length }} 件
      </el-tag>
    </div>

    <div v-if="data.length > 0" class="table-wrapper">
      <el-table
        :data="data"
        class="expiring-table"
        :header-cell-style="{ color: 'var(--text-primary)', fontWeight: 600, background: 'var(--primary-10)' }"
        max-height="400"
      >
        <el-table-column label="物品名称" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="item-name-cell">
              <div class="item-icon-sm">
                <i class="iconfont icon-item" />
              </div>
              <span class="item-name">{{ row.itemName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="所在冰箱" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="fridge-name">{{ row.fridgeName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="数量" width="100" align="center">
          <template #default="{ row }">
            <span class="quantity">{{ row.itemNum }} {{ row.unitName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="保质期" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.shelfLifeDays" class="shelf-life">{{ row.shelfLifeDays }} 天</span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column label="剩余天数" width="100" align="center">
          <template #default="{ row }">
            <span
              class="remaining-days"
              :class="getRemainingClass(row.remainingDays)"
            >
              {{ row.remainingDays <= 0 ? '已过期' : row.remainingDays + ' 天' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="row.freshnessType"
              :effect="themeStore.theme === 'dark' ? 'dark' : 'light'"
            >
              {{ row.freshnessLabel }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <CustomButton type="primary" size="small" @click="handleManage(row)" style="margin: 4px 0">
              管理
            </CustomButton>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty v-else description="暂无临期或过期物品，太棒了！" class="chart-empty" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import CustomButton from '@/components/CustomButton.vue'

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const router = useRouter()
const themeStore = useThemeStore()

function getRemainingClass(days) {
  if (days <= 0) return 'expired'
  if (days <= 3) return 'critical'
  return 'warning'
}

function handleManage(row) {
  router.push({
    name: 'fridge-items',
    params: { id: row.fridgeId }
  })
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
  color: var(--warn-color);
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.table-wrapper {
  flex: 1;
  overflow: hidden;
}

.expiring-table {
  --el-table-border-color: var(--gray-40);
  --el-table-row-hover-bg-color: var(--primary-10);
  width: 100%;
  border-radius: var(--radius-md);
}

.item-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-icon-sm {
  width: 28px;
  height: 28px;
  background: var(--primary-light);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .iconfont {
    font-size: 14px;
    color: var(--primary-color);
  }
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.fridge-name {
  font-size: 13px;
  color: var(--text-secondary);
}

.quantity {
  font-size: 14px;
  color: var(--primary-color);
  font-weight: 600;
}

.shelf-life {
  font-size: 13px;
  color: var(--success-color);
  font-weight: 500;
}

.remaining-days {
  font-size: 13px;
  font-weight: 600;

  &.expired {
    color: var(--danger-color);
  }

  &.critical {
    color: var(--warn-color);
  }

  &.warning {
    color: var(--text-secondary);
  }
}

.empty-text {
  font-size: 13px;
  color: var(--text-tertiary);
}

.chart-empty {
  flex: 1;
  min-height: 200px;
}

@media (max-width: 768px) {
  .chart-card {
    padding: var(--space-4);
  }

  .expiring-table {
    font-size: 12px;
  }
}
</style>
