<template>
  <div class="achievement-panel" :class="{ 'is-collapsed': isCollapsed }">
    <div class="panel-header">
      <div class="panel-title">
        <i class="iconfont icon-Trophy"/>
        <span>成就概览</span>
      </div>
      <div class="panel-actions">
        <button
            class="panel-action-btn"
            title="成就设置"
            @click="handleSettingsClick"
        >
          <i class="iconfont icon-sliders"/>
        </button>
        <button
            class="panel-action-btn"
            :title="isCollapsed ? '展开' : '折叠'"
            @click="handleToggleCollapse"
        >
          <i class="iconfont" :class="isCollapsed ? 'icon-arrow-down' : 'icon-arrow-up'"/>
        </button>
      </div>
    </div>

    <!-- 折叠态 -->
    <div v-if="isCollapsed" class="panel-collapsed">
      <i class="iconfont icon-Trophy"/>
      <span>成就面板已折叠，点击右上角展开</span>
    </div>

    <!-- 加载态 -->
    <div v-else-if="loading" class="panel-loading">
      <el-skeleton :rows="4" animated/>
    </div>

    <!-- 正常态 -->
    <div v-else class="panel-content">
      <div class="panel-grid">
        <!-- 等级 + EXP 进度 -->
        <div class="panel-card level-exp-card">
          <LevelExpCard
              :level="overview?.level?.currentLevel"
              :title="overview?.level?.title"
              :total-exp="overview?.level?.totalExp"
              :icons="overview?.level?.icons"
              :current-exp="overview?.level?.currentExp"
              :required-exp="overview?.level?.requiredExp"
              :today-exp="overview?.todayExp"
              :today-exp-limit="overview?.todayExpLimit"
          />
        </div>

        <!-- 冰鲜连续 -->
        <div class="panel-card streak-card">
          <StreakCard
              :current-streak="overview?.streak?.currentStreak"
              :max-streak="overview?.streak?.maxStreak"
              :protect-remaining="overview?.streak?.protectRemaining"
              :protect-total="overview?.streak?.protectTotal"
              :protect-auto-enabled="overview?.streak?.protectAutoEnabled"
              :melt-warning="overview?.streak?.meltWarning"
          />
        </div>

        <!-- 保鲜评分 -->
        <div class="panel-card score-card">
          <FreshnessScoreCard
              :score="overview?.freshnessScore"
              :grade="overview?.scoreGrade"
              :dimensions="overview?.freshnessDimensions"
              @click="handleScoreClick"
          />
        </div>
      </div>

      <!-- 最近徽章 -->
      <div v-if="recentBadges.length > 0" class="recent-badges">
        <div class="section-title">最近徽章</div>
        <div class="badges-row">
          <div
              v-for="badge in recentBadges"
              :key="badge.code"
              class="recent-badge"
              :title="badge.name"
              @click="handleBadgeClick"
          >
            <i class="iconfont" :class="badge.iconClass"/>
          </div>
        </div>
      </div>

      <!-- 近 90 天保鲜趋势热力图 -->
      <div class="heatmap-preview">
        <div class="section-title">
          <span>近一年保鲜趋势</span>
          <button class="text-link" @click="handleHeatmapExpand">查看月度报告</button>
        </div>
        <FreshnessHeatmap :data="overview?.heatmap || []"/>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import LevelExpCard from './LevelExpCard.vue'
import StreakCard from './StreakCard.vue'
import FreshnessScoreCard from './FreshnessScoreCard.vue'
import FreshnessHeatmap from './FreshnessHeatmap.vue'

const props = defineProps({
  overview: {
    type: Object,
    default: null
  },
  settings: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'settings-click',
  'toggle-collapse',
  'tab-change',
  'score-click'
])

const isCollapsed = computed(() => !!props.settings?.panelHidden)

const recentBadges = computed(() => {
  if (props.overview?.recentBadges?.length) {
    return props.overview.recentBadges.slice(0, 6)
  }
  // 若后端未返回 recentBadges，则从 badges 中派生最近解锁的徽章
  const badges = props.overview?.badges || []
  return [...badges]
      .filter((badge) => badge.unlocked)
      .sort((a, b) => new Date(b.unlockedAt || 0) - new Date(a.unlockedAt || 0))
      .slice(0, 6)
})

const handleSettingsClick = () => {
  emit('settings-click')
}

const handleToggleCollapse = () => {
  emit('toggle-collapse')
}

const handleBadgeClick = () => {
  emit('tab-change', 'badges')
}

const handleHeatmapExpand = () => {
  emit('tab-change', 'reports')
}

const handleScoreClick = () => {
  emit('score-click')
}
</script>

<style scoped lang="scss">
.achievement-panel {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  padding: var(--space-5) var(--space-6);
  transition: all 0.3s ease;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: stretch;
  height: 100%;

  &:hover {
    box-shadow: var(--shadow-lg);
  }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);

  .iconfont {
    font-size: 22px;
    color: var(--badge-gold-icon);
  }
}

.panel-actions {
  display: flex;
  gap: var(--space-2);
}

.panel-action-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: var(--input-bg);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;

  &:hover {
    background: var(--primary-20);
    color: var(--primary-color);
  }

  .iconfont {
    font-size: 14px;
  }
}

.panel-collapsed {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-6);
  color: var(--text-secondary);
  font-size: 14px;

  .iconfont {
    font-size: 20px;
    color: var(--badge-gold-icon);
  }
}

.panel-loading {
  padding: var(--space-4) 0;
}

.panel-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  min-width: 0;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-5);
}

.panel-card {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: var(--shadow-sm);
    transform: translateY(-1px);
  }
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
}

.text-link {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 13px;
  cursor: pointer;
  padding: 0;

  &:hover {
    text-decoration: underline;
  }
}

.recent-badges {
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-color);
}

.badges-row {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.recent-badge {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--badge-gold-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: scale(1.1);
    box-shadow: var(--badge-gold-shadow-hover);
  }

  .iconfont {
    font-size: 22px;
    color: var(--badge-gold-icon);
  }
}

.heatmap-preview {
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-color);
  min-width: 0;
}

@media (max-width: 1024px) {
  .panel-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .achievement-panel {
    padding: var(--space-4) var(--space-5);
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
