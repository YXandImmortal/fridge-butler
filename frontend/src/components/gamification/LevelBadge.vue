<template>
  <div class="level-badge" :title="tooltipText">
    <div class="level-icons">
      <template v-for="icon in displayIcons" :key="icon.key">
        <SvgIcon
            v-for="n in icon.count"
            :key="`${icon.key}-${n}`"
            :name="icon.name"
            :size="28"
            class="level-icon"
        />
      </template>
    </div>
    <div class="level-info">
      <span class="level-text">Lv.{{ level }}</span>
      <span class="level-title">{{ title }}</span>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import SvgIcon from '@/components/ui/SvgIcon.vue'

const props = defineProps({
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
  }
})

// 后端返回的 icons 为数量，如 {snowman:0, iceCream:0, ice:0, snowflake:1}
const iconConfig = [
  {key: 'snowman', label: '雪人', name: 'level-snowman'},
  {key: 'iceCream', label: '冰淇淋', name: 'level-ice-cream'},
  {key: 'ice', label: '冰块', name: 'level-ice'},
  {key: 'snowflake', label: '雪花', name: 'level-snowflakes'}
]

const displayIcons = computed(() => {
  return iconConfig
      .map(item => ({
        ...item,
        count: Math.max(0, parseInt(props.icons?.[item.key], 10) || 0)
      }))
      .filter(item => item.count > 0)
})

const tooltipText = computed(() => {
  return `${props.title || ''} Lv.${props.level} | 累计 EXP ${props.totalExp || 0}`
})
</script>

<style scoped lang="scss">
.level-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-3);
  cursor: default;
}

.level-icons {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.level-icon {
  transition: transform 0.3s ease;
}

.level-badge:hover .level-icon {
  transform: translateY(-2px);
}

.level-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.level-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.level-title {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
