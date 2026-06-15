<template>
  <div class="badge-section">
    <div v-if="loading" class="section-loading">
      <el-skeleton :rows="4" animated/>
    </div>
    <div v-else-if="badges.length === 0" class="empty-state">
      <i class="iconfont icon-Medal"/>
      <span>暂无徽章数据</span>
    </div>
    <div v-else class="badge-grid">
      <div
          v-for="badge in badges"
          :key="badge.code"
          class="badge-item"
          :class="{ 'is-locked': !badge.unlocked }"
          :title="badge.unlocked ? badge.description : badge.unlockConditionDesc"
      >
        <div class="badge-icon-wrapper">
          <i class="iconfont" :class="badge.iconClass || 'icon-Medal'"/>
          <div v-if="badge.unlocked" class="badge-unlocked-mark">
            <i class="iconfont icon-check"/>
          </div>
        </div>
        <div class="badge-info">
          <div class="badge-name">{{ badge.name }}</div>
          <div class="badge-desc">{{ badge.unlocked ? badge.description : badge.unlockConditionDesc }}</div>
          <div class="badge-reward">+{{ badge.expReward }} EXP</div>
        </div>
        <div v-if="badge.unlocked && badge.unlockedAt" class="badge-time">
          {{ formatUnlockTime(badge.unlockedAt) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted} from 'vue'
import {useGamificationStore} from '@/stores/gamification'

const gamificationStore = useGamificationStore()

const badges = computed(() => gamificationStore.badges)
const loading = computed(() => gamificationStore.isBadgesLoading)

onMounted(() => {
  gamificationStore.fetchBadges()
})

const formatUnlockTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace(' ', 'T'))
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped lang="scss">
.badge-section {
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
    color: var(--badge-gold-icon);
  }
}

.badge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-4);
}

.badge-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
  cursor: default;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-sm);
  }

  &.is-locked {
    opacity: 0.55;
    background: var(--input-bg);

    .badge-icon-wrapper {
      filter: grayscale(100%);
    }

    .badge-desc {
      color: var(--text-tertiary);
    }
  }
}

.badge-icon-wrapper {
  position: relative;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: var(--badge-gold-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .iconfont {
    font-size: 28px;
    color: var(--badge-gold-icon);
  }
}

.badge-unlocked-mark {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--success-color);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--card-bg);

  .iconfont {
    font-size: 12px;
    color: #fff;
  }
}

.badge-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.badge-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.badge-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.badge-reward {
  font-size: 12px;
  font-weight: 600;
  color: var(--badge-gold-icon);
}

.badge-time {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  font-size: 11px;
  color: var(--text-tertiary);
}

@media (max-width: 768px) {
  .badge-grid {
    grid-template-columns: 1fr;
  }
}
</style>
