<template>
  <div
    class="custom-switch"
    :class="switchClasses"
    :style="switchStyle"
    role="switch"
    :aria-checked="isChecked"
    :aria-disabled="disabled || loading"
    tabindex="0"
    @click="handleToggle"
    @keydown.space.prevent="handleToggle"
    @keydown.enter.prevent="handleToggle"
  >
    <!-- 前置文本标签 -->
    <span
      v-if="inactiveText && !inlinePrompt"
      class="cs-label cs-label--inactive"
      :class="{ 'is-active': !isChecked }"
    >
      {{ inactiveText }}
    </span>

    <!-- 开关轨道 -->
    <div class="cs-track" :style="trackStyle">
      <!-- 轨道内部文字/图标（inline-prompt 模式） -->
      <span
        v-if="inlinePrompt && activeText"
        class="cs-inline-text cs-inline-text--active"
      >{{ activeText }}</span>
      <span
        v-if="inlinePrompt && inactiveText"
        class="cs-inline-text cs-inline-text--inactive"
      >{{ inactiveText }}</span>

      <!-- 滑块按钮 -->
      <div class="cs-thumb" :style="thumbStyle">
        <i v-if="loading" class="iconfont icon-loader cs-loading-icon"/>
        <span
          v-else-if="inlinePrompt && (activeIcon || inactiveIcon)"
          class="iconfont"
          :class="isChecked ? activeIcon : inactiveIcon"
        />
      </div>
    </div>

    <!-- 后置文本标签 -->
    <span
      v-if="activeText && !inlinePrompt"
      class="cs-label cs-label--active"
      :class="{ 'is-active': isChecked }"
    >
      {{ activeText }}
    </span>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: [Boolean, String, Number],
    default: false
  },
  activeValue: {
    type: [Boolean, String, Number],
    default: true
  },
  inactiveValue: {
    type: [Boolean, String, Number],
    default: false
  },
  activeText: {
    type: String,
    default: ''
  },
  inactiveText: {
    type: String,
    default: ''
  },
  activeIcon: {
    type: String,
    default: ''
  },
  inactiveIcon: {
    type: String,
    default: ''
  },
  inlinePrompt: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'large', 'small'].includes(val)
  },
  activeColor: {
    type: String,
    default: ''
  },
  inactiveColor: {
    type: String,
    default: ''
  },
  width: {
    type: [String, Number],
    default: ''
  },
  beforeChange: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isChanging = ref(false)

const isChecked = computed(() => {
  return props.modelValue === props.activeValue
})

const switchClasses = computed(() => {
  const classes = [`cs-size--${props.size}`]
  if (isChecked.value) classes.push('is-checked')
  if (props.disabled || props.loading) classes.push('is-disabled')
  if (props.loading) classes.push('is-loading')
  if (props.inlinePrompt) classes.push('is-inline')
  if (props.activeText || props.inactiveText) classes.push('has-label')
  return classes
})

const switchStyle = computed(() => {
  const style = {}
  if (props.width) {
    style['--cs-track-width'] = typeof props.width === 'number' ? `${props.width}px` : props.width
  }
  return style
})

const trackStyle = computed(() => {
  const style = {}
  if (isChecked.value) {
    style.backgroundColor = props.activeColor || 'var(--primary-color)'
    style.boxShadow = `0 2px 8px ${props.activeColor ? `${props.activeColor}66` : 'var(--primary-40)'}`
  } else {
    style.backgroundColor = props.inactiveColor || 'var(--border-color)'
    style.boxShadow = 'var(--shadow-input)'
  }
  return style
})

const thumbStyle = computed(() => {
  return {}
})

async function handleToggle() {
  if (props.disabled || props.loading || isChanging.value) return

  const newChecked = !isChecked.value
  const newValue = newChecked ? props.activeValue : props.inactiveValue

  if (props.beforeChange) {
    isChanging.value = true
    try {
      const result = await props.beforeChange(newValue)
      if (result === false) {
        isChanging.value = false
        return
      }
    } catch {
      isChanging.value = false
      return
    }
    isChanging.value = false
  }

  emit('update:modelValue', newValue)
  emit('change', newValue)
}
</script>

<style scoped lang="scss">
/* =========================================================
 * CustomSwitch — 项目风格开关组件
 * 替代 el-switch，完全可控的样式与交互
 * ========================================================= */

.custom-switch {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  user-select: none;
  font-family: var(--el-font-family);
  vertical-align: middle;
  outline: none;
  -webkit-tap-highlight-color: transparent;
}

/* ---------- 轨道 ---------- */
.cs-track {
  position: relative;
  width: var(--cs-track-width, 44px);
  height: 24px;
  border-radius: 9999px;
  background-color: var(--border-color);
  box-shadow: var(--shadow-input);
  transition: background-color 0.35s ease, box-shadow 0.35s ease;
  flex-shrink: 0;
  overflow: hidden;
}

/* ---------- 滑块 ---------- */
.cs-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--text-inverse);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1),
              width 0.25s ease,
              left 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

/* 开启状态：滑块右移 */
.custom-switch.is-checked .cs-thumb {
  transform: translateX(20px);
}

/* 拖拽/按下时滑块变宽效果（通过 active 状态模拟） */
.custom-switch:active:not(.is-disabled) .cs-thumb {
  width: 24px;
}

/* ---------- 加载动画 ---------- */
.cs-loading-icon {
  font-size: 12px;
  color: var(--text-tertiary);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ---------- 文本标签 ---------- */
.cs-label {
  font-size: 14px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
  white-space: nowrap;
}

.cs-label.is-active {
  color: var(--primary-color);
  font-weight: 500;
}

/* ---------- inline-prompt 模式 ---------- */
.cs-inline-text {
  position: absolute;
  top: 0;
  height: 100%;
  display: flex;
  align-items: center;
  font-size: 10px;
  font-weight: 500;
  color: var(--text-inverse);
  opacity: 0;
  transition: opacity 0.25s ease;
  pointer-events: none;
  white-space: nowrap;
  z-index: 1;
}

.cs-inline-text--active {
  right: 6px;
}

.cs-inline-text--inactive {
  left: 6px;
  color: var(--text-tertiary);
}

.custom-switch.is-checked .cs-inline-text--active {
  opacity: 1;
}

.custom-switch:not(.is-checked) .cs-inline-text--inactive {
  opacity: 1;
}

/* ---------- 尺寸系统 ---------- */

/* default */
.cs-size--default .cs-track {
  width: var(--cs-track-width, 44px);
  height: 24px;
}

.cs-size--default .cs-thumb {
  width: 20px;
  height: 20px;
}

.cs-size--default.is-checked .cs-thumb {
  transform: translateX(20px);
}

.cs-size--default:active:not(.is-disabled) .cs-thumb {
  width: 24px;
}

.cs-size--default .cs-label {
  font-size: 14px;
}

/* large */
.cs-size--large .cs-track {
  width: var(--cs-track-width, 56px);
  height: 30px;
}

.cs-size--large .cs-thumb {
  width: 26px;
  height: 26px;
}

.cs-size--large.is-checked .cs-thumb {
  transform: translateX(26px);
}

.cs-size--large:active:not(.is-disabled) .cs-thumb {
  width: 30px;
}

.cs-size--large .cs-label {
  font-size: 16px;
}

.cs-size--large .cs-inline-text {
  font-size: 12px;
}

/* small */
.cs-size--small .cs-track {
  width: var(--cs-track-width, 36px);
  height: 20px;
}

.cs-size--small .cs-thumb {
  width: 16px;
  height: 16px;
}

.cs-size--small.is-checked .cs-thumb {
  transform: translateX(16px);
}

.cs-size--small:active:not(.is-disabled) .cs-thumb {
  width: 20px;
}

.cs-size--small .cs-label {
  font-size: 12px;
}

.cs-size--small .cs-inline-text {
  font-size: 9px;
}

/* ---------- 禁用状态 ---------- */
.custom-switch.is-disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.custom-switch.is-disabled .cs-track {
  box-shadow: none !important;
}

/* ---------- focus-visible 聚焦环 ---------- */
.custom-switch:focus-visible .cs-track {
  outline: none;
  box-shadow: 0 0 0 3px var(--primary-40);
}

.custom-switch.is-checked:focus-visible .cs-track {
  box-shadow: 0 0 0 3px var(--primary-40), 0 2px 8px var(--primary-40);
}
</style>
