<template>
  <div class="monthly-report-section">
    <!-- 月份切换 -->
    <div class="report-header">
      <CustomButton
          type="default"
          size="small"
          :disabled="!canGoPrev"
          @click="changeMonth(-1)"
      >
        <i class="iconfont icon-arrow-left"/>
        上月
      </CustomButton>
      <div class="report-month">
        <i class="iconfont icon-calendar"/>
        {{ displayYearMonth }}
      </div>
      <CustomButton
          type="default"
          size="small"
          :disabled="!canGoNext"
          @click="changeMonth(1)"
      >
        下月
        <i class="iconfont icon-arrow-right"/>
      </CustomButton>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="section-loading">
      <el-skeleton :rows="6" animated/>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!report" class="empty-state">
      <i class="iconfont icon-calendar"/>
      <span>该月暂无报告数据</span>
    </div>

    <!-- 报告内容 -->
    <template v-else>
      <!-- 评分概览 -->
      <div class="score-overview">
        <div class="score-card main-score" :class="`grade--${scoreGrade}`">
          <div class="score-label">月平均保鲜评分</div>
          <div class="score-value">{{ report.avgScore ?? '-' }}</div>
          <div class="score-grade">{{ scoreGrade }}</div>
        </div>
        <div class="score-card">
          <div class="score-label">最高评分</div>
          <div class="score-value">{{ report.maxScore ?? '-' }}</div>
        </div>
        <div class="score-card">
          <div class="score-label">最低评分</div>
          <div class="score-value">{{ report.minScore ?? '-' }}</div>
        </div>
      </div>

      <!-- 等级变化 -->
      <div v-if="report.levelChange" class="level-change-banner">
        <i class="iconfont icon-trending"/>
        <span>{{ report.levelChange }}</span>
      </div>

      <!-- 统计网格 -->
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-icon expired">
            <i class="iconfont icon-warning-box"/>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ report.expiredCount ?? 0 }}</div>
            <div class="stat-label">过期食材</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon near-expired">
            <i class="iconfont icon-calendar-alert"/>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ report.nearExpiredCount ?? 0 }}</div>
            <div class="stat-label">临期食材</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon wasted">
            <i class="iconfont icon-money"/>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatAmount(report.wastedAmount) }}</div>
            <div class="stat-label">浪费金额估算</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon eco">
            <i class="iconfont icon-leaf"/>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatAmount(report.ecoValue) }}</div>
            <div class="stat-label">环保价值估算</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon in-out">
            <i class="iconfont icon-add-box"/>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ report.itemInCount ?? 0 }}</div>
            <div class="stat-label">入库次数</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon in-out">
            <i class="iconfont icon-logout"/>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ report.itemOutCount ?? 0 }}</div>
            <div class="stat-label">取出次数</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon streak">
            <i class="iconfont icon-calendar-month"/>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ report.maxStreak ?? 0 }}<span class="unit">天</span></div>
            <div class="stat-label">本月最高连续</div>
          </div>
        </div>
      </div>

      <!-- 新获得徽章 -->
      <div v-if="newBadges.length > 0" class="new-badges">
        <div class="section-title">
          <i class="iconfont icon-Medal"/>
          本月新获得徽章
        </div>
        <div class="badge-list">
          <div
              v-for="badge in newBadges"
              :key="badge.code"
              class="badge-card"
          >
            <div class="badge-icon">
              <i class="iconfont" :class="badge.iconClass || 'icon-Medal'"/>
            </div>
            <div class="badge-name">{{ badge.name }}</div>
            <div class="badge-reward">+{{ badge.expReward }} EXP</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useGamificationStore, getScoreGrade} from '@/stores/gamification'
import {viewMonthlyReport} from '@/api/gamification'
import CustomButton from '@/components/ui/CustomButton.vue'
import notifyGamificationResult from '@/utils/gamificationNotify'

const gamificationStore = useGamificationStore()

// 本会话内已完成奖励结算的月份，避免重复调用结算接口
const viewedMonths = new Set()

const currentYearMonth = ref('')
const loading = computed(() => gamificationStore.loading.monthlyReport)
const report = computed(() => gamificationStore.monthlyReports[currentYearMonth.value])
const scoreGrade = computed(() => {
  const grade = getScoreGrade(report.value?.avgScore)
  return grade ? grade.label : '-'
})
const newBadges = computed(() => report.value?.newBadges || [])

const displayYearMonth = computed(() => {
  if (!currentYearMonth.value) return ''
  const [year, month] = currentYearMonth.value.split('-')
  return `${year}年${parseInt(month, 10)}月`
})

const canGoPrev = computed(() => {
  if (!currentYearMonth.value) return false
  const [year, month] = currentYearMonth.value.split('-').map(Number)
  const prev = new Date(year, month - 2, 1)
  // 最多回溯到 2024-01
  return prev.getFullYear() > 2023 || (prev.getFullYear() === 2024 && prev.getMonth() >= 0)
})

const canGoNext = computed(() => {
  if (!currentYearMonth.value) return false
  const [year, month] = currentYearMonth.value.split('-').map(Number)
  const next = new Date(year, month, 1)
  const now = new Date()
  return next.getFullYear() < now.getFullYear() ||
      (next.getFullYear() === now.getFullYear() && next.getMonth() <= now.getMonth())
})

onMounted(() => {
  const now = new Date()
  // 后端每月 1 日生成上月报告，默认展示上月以确保有数据
  const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
  currentYearMonth.value = `${lastMonth.getFullYear()}-${String(lastMonth.getMonth() + 1).padStart(2, '0')}`
  loadReport(true)
})

const loadReport = async (shouldNotify = false) => {
  if (!currentYearMonth.value) return

  // 1. 先查询月度报告数据并渲染
  const data = await gamificationStore.fetchMonthlyReport(currentYearMonth.value)

  // 2. 报告存在且需要通知时，调用专用结算接口触发 EXP/升级弹窗
  if (shouldNotify && data && !viewedMonths.has(currentYearMonth.value)) {
    try {
      const rewardRes = await viewMonthlyReport(currentYearMonth.value)
      if (rewardRes.code === 200 && rewardRes.data) {
        viewedMonths.add(currentYearMonth.value)
        notifyGamificationResult(rewardRes, '查看月度报告')
      }
    } catch (err) {
      console.error('月度报告结算失败:', err)
    }
  }
}

const changeMonth = (delta) => {
  const [year, month] = currentYearMonth.value.split('-').map(Number)
  const next = new Date(year, month - 1 + delta, 1)
  currentYearMonth.value = `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}`
  loadReport(true)
}

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}
</script>

<style scoped lang="scss">
.monthly-report-section {
  min-height: 200px;
}

.report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--divider-color);
}

.report-month {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);

  .iconfont {
    color: var(--primary-color);
  }
}

.section-loading {
  padding: var(--space-4) 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  padding: var(--space-12) 0;
  color: var(--text-tertiary);
  font-size: 14px;

  .iconfont {
    font-size: 48px;
    color: var(--success-color);
  }
}

.score-overview {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.score-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: var(--space-5);
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-sm);
  }

  .score-label {
    font-size: 13px;
    color: var(--text-secondary);
  }

  .score-value {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
  }
}

.score-card.main-score {
  border-width: 2px;

  &.grade--S {
    border-color: var(--gamification-s);
    background: var(--gamification-s-bg);
    .score-value, .score-grade { color: var(--gamification-s); }
  }

  &.grade--A {
    border-color: var(--gamification-a);
    background: var(--gamification-a-bg);
    .score-value, .score-grade { color: var(--gamification-a); }
  }

  &.grade--B {
    border-color: var(--gamification-b);
    background: var(--gamification-b-bg);
    .score-value, .score-grade { color: var(--gamification-b); }
  }

  &.grade--C {
    border-color: var(--gamification-c);
    background: var(--gamification-c-bg);
    .score-value, .score-grade { color: var(--gamification-c); }
  }

  &.grade--D {
    border-color: var(--gamification-d);
    background: var(--gamification-d-bg);
    .score-value, .score-grade { color: var(--gamification-d); }
  }

  .score-grade {
    font-size: 14px;
    font-weight: 700;
  }
}

.level-change-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-4);
  margin-bottom: var(--space-6);
  background: var(--primary-10);
  border: 1px solid var(--primary-30);
  border-radius: var(--radius-md);
  color: var(--primary-color);
  font-size: 14px;
  font-weight: 600;

  .iconfont {
    font-size: 18px;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-sm);
  }
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .iconfont {
    font-size: 22px;
    color: #fff;
  }

  &.expired { background: var(--gamification-d); }
  &.near-expired { background: var(--gamification-c); }
  &.wasted { background: var(--warn-color); }
  &.eco { background: var(--gamification-s); }
  &.in-out { background: var(--gamification-a); }
  &.streak { background: var(--gamification-b); }
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);

  .unit {
    font-size: 12px;
    font-weight: 400;
    color: var(--text-secondary);
    margin-left: 2px;
  }
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.new-badges {
  .section-title {
    display: flex;
    align-items: center;
    gap: var(--space-2);
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: var(--space-4);

    .iconfont {
      color: var(--badge-gold-icon);
    }
  }
}

.badge-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.badge-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4);
  min-width: 120px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-sm);
  }
}

.badge-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--badge-gold-bg);
  display: flex;
  align-items: center;
  justify-content: center;

  .iconfont {
    font-size: 26px;
    color: var(--badge-gold-icon);
  }
}

.badge-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.badge-reward {
  font-size: 12px;
  font-weight: 600;
  color: var(--badge-gold-icon);
}

@media (max-width: 768px) {
  .score-overview {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .report-header {
    flex-wrap: wrap;
  }
}
</style>
