<template>
  <section class="stats-row">
    <div
        v-for="(stat, index) in statsList"
        :key="stat.key"
        class="stat-card glass-card animate-in"
        :style="{ animationDelay: `${0.1 + index * 0.08}s` }"
    >
      <div class="stat-icon-wrapper" :style="{ background: stat.iconBg }">
        <i :class="['iconfont', stat.icon, 'stat-icon']" :style="{ color: stat.iconColor }"/>
      </div>
      <div class="stat-info">
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
  fridgeList: {
    type: Array,
    default: () => []
  },
  itemList: {
    type: Array,
    default: () => []
  },
  takeOutList: {
    type: Array,
    default: () => []
  },
  expiringSummary: {
    type: Object,
    default: () => ({expiringCount: 0, expiredCount: 0, totalExpiring: 0})
  }
})

const statsList = computed(() => {
  const fridgeCount = props.fridgeList.length

  const totalItemNum = props.itemList.length

  const now = new Date()
  const sevenDaysAgo = new Date(now)
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 6)
  sevenDaysAgo.setHours(0, 0, 0, 0)

  const recentTakeOut = props.takeOutList.reduce((sum, item) => {
    const itemDate = new Date(item.date)
    return itemDate >= sevenDaysAgo ? sum + (item.count || 0) : sum
  }, 0)

  const expiring = props.expiringSummary.totalExpiring || 0

  return [
    {
      key: 'fridge',
      value: fridgeCount,
      label: '我的冰箱',
      icon: 'icon-fridge-line',
      iconBg: 'linear-gradient(135deg, rgba(100,181,246,0.15) 0%, rgba(129,212,250,0.1) 100%)',
      iconColor: '#64B5F6'
    },
    {
      key: 'items',
      value: totalItemNum,
      label: '物品总数',
      icon: 'icon-item',
      iconBg: 'linear-gradient(135deg, rgba(129,199,132,0.15) 0%, rgba(165,214,167,0.1) 100%)',
      iconColor: '#81C784'
    },
    {
      key: 'takeout',
      value: recentTakeOut,
      label: '7天取出',
      icon: 'icon-arrow-up-box',
      iconBg: 'linear-gradient(135deg, rgba(255,183,77,0.15) 0%, rgba(255,202,128,0.1) 100%)',
      iconColor: '#FFB74D'
    },
    {
      key: 'expiring',
      value: expiring,
      label: '临期提醒',
      icon: 'icon-calendar-alert',
      iconBg: 'linear-gradient(135deg, rgba(248,113,113,0.15) 0%, rgba(239,154,154,0.1) 100%)',
      iconColor: '#F87171'
    }
  ]
})
</script>

<style scoped lang="scss">
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-5);
  cursor: default;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon {
  font-size: 24px;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
}

.animate-in {
  opacity: 0;
  animation: fade-in-up 0.6s ease-out forwards;
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

@media (max-width: 992px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-3);
  }

  .stat-card {
    padding: var(--space-4);
  }

  .stat-icon-wrapper {
    width: 40px;
    height: 40px;
  }

  .stat-icon {
    font-size: 20px;
  }

  .stat-value {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .stats-row {
    grid-template-columns: 1fr 1fr;
    gap: var(--space-3);
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    gap: var(--space-3);
  }
}
</style>
