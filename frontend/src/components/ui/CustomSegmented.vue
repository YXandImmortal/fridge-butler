<template>
  <div
      class="custom-segmented"
      :class="[
        `custom-segmented--${size}`,
        {
          'is-block': block,
          'is-round': round,
          'is-disabled': disabled
        }
      ]"
      role="tablist"
  >
    <!-- 滑动指示器 -->
    <div
        v-if="activeOption"
        class="custom-segmented__indicator"
        :style="indicatorStyle"
        aria-hidden="true"
    />

    <!-- Tab 选项 -->
    <div
        v-for="(option, index) in options"
        :key="option.value"
        :ref="el => setItemRef(el, index)"
        class="custom-segmented__item"
        :class="{
          'is-active': modelValue === option.value,
          'is-disabled': disabled || option.disabled
        }"
        role="tab"
        :aria-selected="modelValue === option.value"
        :tabindex="disabled || option.disabled ? -1 : 0"
        @click="handleSelect(option)"
        @keydown.enter.space.prevent="handleSelect(option)"
    >
      <i v-if="option.icon" class="iconfont" :class="option.icon"/>
      <span>{{ option.label }}</span>
    </div>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, onUnmounted, ref, watch} from 'vue'

const props = defineProps({
  /**
   * 当前选中的值
   */
  modelValue: {
    type: [String, Number],
    required: true
  },
  /**
   * 选项列表
   * @type {Array<{ label: string, value: string|number, icon?: string, disabled?: boolean }>}
   */
  options: {
    type: Array,
    default: () => []
  },
  /**
   * 尺寸：small / default / large
   */
  size: {
    type: String,
    default: 'default'
  },
  /**
   * 是否撑满容器宽度，每个 Tab 等分
   */
  block: {
    type: Boolean,
    default: false
  },
  /**
   * 是否整体禁用
   */
  disabled: {
    type: Boolean,
    default: false
  },
  /**
   * 是否使用胶囊圆角（9999px），false 时使用 --radius-md
   */
  round: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const itemRefs = ref([])
const indicatorOffset = ref(0)
const indicatorWidth = ref(0)

const activeOption = computed(() => props.options.find(o => o.value === props.modelValue))

const setItemRef = (el, index) => {
  if (el) {
    itemRefs.value[index] = el
  }
}

const updateIndicator = async () => {
  await nextTick()
  const activeIndex = props.options.findIndex(o => o.value === props.modelValue)
  const activeEl = itemRefs.value[activeIndex]
  if (!activeEl) {
    indicatorWidth.value = 0
    return
  }
  indicatorOffset.value = activeEl.offsetLeft
  indicatorWidth.value = activeEl.offsetWidth
}

const indicatorStyle = computed(() => ({
  width: `${indicatorWidth.value}px`,
  transform: `translateX(${indicatorOffset.value}px)`
}))

const handleSelect = (option) => {
  if (props.disabled || option.disabled) return
  if (option.value === props.modelValue) return

  emit('update:modelValue', option.value)
  emit('change', option.value, option)
}

watch(() => props.modelValue, updateIndicator)
watch(() => props.options, updateIndicator, {deep: true})

onMounted(() => {
  updateIndicator()
  window.addEventListener('resize', updateIndicator)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIndicator)
})
</script>

<style scoped lang="scss">
.custom-segmented {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  user-select: none;
  transition: box-shadow 0.3s ease;

  /* 尺寸变体 */
  &--small {
    .custom-segmented__item {
      padding: 6px 14px;
      font-size: 13px;
      gap: 4px;
    }
  }

  &--default {
    .custom-segmented__item {
      padding: 8px 20px;
      font-size: 14px;
      gap: var(--space-2);
    }
  }

  &--large {
    .custom-segmented__item {
      padding: 10px 28px;
      font-size: 15px;
      gap: var(--space-2);
    }
  }

  /* 撑满容器 */
  &.is-block {
    display: flex;
    width: 100%;

    .custom-segmented__item {
      flex: 1;
      justify-content: center;
    }
  }

  /* 圆角变体 */
  &.is-round {
    border-radius: 9999px;

    .custom-segmented__indicator,
    .custom-segmented__item {
      border-radius: 9999px;
    }
  }

  &:not(.is-round) {
    border-radius: var(--radius-md);

    .custom-segmented__indicator,
    .custom-segmented__item {
      border-radius: var(--radius-md);
    }
  }

  &:hover:not(.is-disabled) {
    box-shadow: var(--shadow-md);
  }

  /* 禁用态 */
  &.is-disabled {
    opacity: 0.65;
    cursor: not-allowed;

    .custom-segmented__item {
      cursor: not-allowed;
    }
  }
}

/* 滑动指示器 */
.custom-segmented__indicator {
  position: absolute;
  top: 4px;
  bottom: 4px;
  left: 0;
  background: var(--primary-10);
  box-shadow: var(--shadow-sm);
  transition:
      transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1),
      width 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 0;
}

/* Tab 选项 */
.custom-segmented__item {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.25s ease, transform 0.25s ease;
  outline: none;

  .iconfont {
    font-size: 1.1em;
  }

  /* 悬浮：未选中且未禁用 */
  &:hover:not(.is-active):not(.is-disabled) {
    color: var(--primary-color);
    transform: translateY(-1px);
  }

  /* 点击按下 */
  &:active:not(.is-disabled) {
    transform: scale(0.97);
  }

  /* 聚焦环 */
  &:focus-visible {
    box-shadow: 0 0 0 3px var(--primary-40);
  }

  /* 选中态 */
  &.is-active {
    color: var(--primary-color);
    font-weight: 600;
  }

  /* 禁用态 */
  &.is-disabled {
    opacity: 0.65;
    cursor: not-allowed;

    &:hover {
      color: var(--text-secondary);
      transform: none;
    }
  }
}
</style>
