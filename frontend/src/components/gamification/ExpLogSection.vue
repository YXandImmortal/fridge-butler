<template>
  <div class="exp-log-section">
    <div v-if="loading" class="section-loading">
      <el-skeleton :rows="6" animated/>
    </div>
    <div v-else-if="records.length === 0" class="empty-state">
      <i class="iconfont icon-star"/>
      <span>暂无 EXP 记录</span>
    </div>
    <template v-else>
      <div class="exp-log-list">
        <div
            v-for="log in records"
            :key="log.id"
            class="exp-log-item"
        >
          <div class="log-main">
            <div class="log-icon">
              <i class="iconfont" :class="getActionIcon(log.actionType)"/>
            </div>
            <div class="log-info">
              <div class="log-title">
                <span class="log-action">{{ getActionLabel(log) }}</span>
                <span v-if="log.actionDesc && log.actionDesc !== getActionLabel(log)" class="log-desc">
                  · {{ log.actionDesc }}
                </span>
              </div>
              <div class="log-time">{{ formatTime(log.createdAt) }}</div>
            </div>
          </div>
          <div class="log-exp">
            <span class="exp-gained">+{{ log.expGained }}</span>
            <span class="exp-balance">总经验值 {{ log.expBalance }}</span>
          </div>
        </div>
      </div>
      <div v-if="pages > 1" class="pagination-wrapper">
        <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useGamificationStore, ACTION_TYPE_MAP} from '@/stores/gamification'

const gamificationStore = useGamificationStore()

const currentPage = ref(1)
const pageSize = ref(20)

const records = computed(() => gamificationStore.expLog.records)
const total = computed(() => gamificationStore.expLog.total)
const pages = computed(() => gamificationStore.expLog.pages)
const loading = computed(() => gamificationStore.isExpLogLoading)

onMounted(() => {
  loadExpLog()
})

const loadExpLog = () => {
  gamificationStore.fetchExpLog({page: currentPage.value, size: pageSize.value})
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadExpLog()
}

const getActionLabel = (log) => {
  return ACTION_TYPE_MAP[log.actionType] || log.actionDesc || log.actionType
}

const getActionIcon = (actionType) => {
  const iconMap = {
    LOGIN: 'icon-login',
    NO_EXPIRE: 'icon-calendar-check',
    CONSUME_EXPIRING: 'icon-logout',
    ADD_ITEM: 'icon-add-box',
    ORGANIZE: 'icon-inbox-all',
    VIEW_DATA_CENTER: 'icon-chart-bar',
    AI_CHAT: 'icon-chat',
    BADGE: 'icon-a-GuaranteeBadge',
    STREAK_BONUS: 'icon-level-ice',
    SCORE_BREAKTHROUGH: 'icon-trending',
    MONTHLY_REPORT: 'icon-calendar-month',
    BIND_EMAIL: 'icon-mail',
    GUIDE: 'icon-teach',
    SHARE: 'icon-external-link'
  }
  return iconMap[actionType] || 'icon-star'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace(' ', 'T'))
  const now = new Date()
  const isToday = date.getDate() === now.getDate() &&
      date.getMonth() === now.getMonth() &&
      date.getFullYear() === now.getFullYear()

  const time = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  if (isToday) {
    return `今天 ${time}`
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${time}`
}
</script>

<style scoped lang="scss">
.exp-log-section {
  min-height: 200px;
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
    color: var(--primary-color);
  }
}

.exp-log-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.exp-log-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all 0.3s ease;

  &:hover {
    background: var(--input-bg);
  }
}

.log-main {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.log-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary-20);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .iconfont {
    font-size: 20px;
    color: var(--primary-color);
  }
}

.log-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.log-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
}

.log-action {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.log-desc {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.log-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.log-exp {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}

.exp-gained {
  font-size: 16px;
  font-weight: 700;
  color: var(--badge-gold-icon);
}

.exp-balance {
  font-size: 11px;
  color: var(--text-tertiary);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: var(--space-6);
}

@media (max-width: 768px) {
  .exp-log-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .log-exp {
    align-items: flex-start;
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
  }
}
</style>
