<template>
  <div class="level-exp-card">
    <div class="level-row">
      <LevelBadge
          :level="level"
          :title="title"
          :total-exp="totalExp"
          :icons="icons"
      />
    </div>
    <div class="exp-row">
      <ExpProgressBar
          :current-exp="currentExp"
          :required-exp="requiredExp"
          :today-exp="todayExp"
          :today-exp-limit="todayExpLimit"
      />
    </div>
  </div>
</template>

<script setup>
import LevelBadge from './LevelBadge.vue'
import ExpProgressBar from './ExpProgressBar.vue'

defineProps({
  level: {
    type: Number,
    default: 1
  },
  title: {
    type: String,
    default: ''
  },
  totalExp: {
    type: Number,
    default: 0
  },
  icons: {
    type: Object,
    default: () => ({})
  },
  currentExp: {
    type: Number,
    default: 0
  },
  requiredExp: {
    type: Number,
    default: 100
  },
  todayExp: {
    type: Number,
    default: 0
  },
  todayExpLimit: {
    type: Number,
    default: 150
  }
})
</script>

<style scoped lang="scss">
.level-exp-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  height: 100%;
  justify-content: center;
}

.level-row {
  display: flex;
  align-items: center;
}

.exp-row {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}
</style>
