<template>
  <label
    class="custom-checkbox"
    :class="checkboxClasses"
    role="checkbox"
    :aria-checked="ariaChecked"
    :aria-disabled="disabled"
    tabindex="0"
    @click="handleClick"
    @keydown.space.prevent="handleClick"
    @keydown.enter.prevent="handleClick"
  >
    <!-- 复选框本体 -->
    <span class="cc-box" :class="boxClasses">
      <transition name="cc-check">
        <i v-if="isChecked && !indeterminate" class="iconfont icon-check cc-icon"/>
        <span v-else-if="indeterminate" class="cc-indeterminate"/>
      </transition>
    </span>

    <!-- 标签文本 -->
    <span v-if="hasLabel" class="cc-label">
      <slot>{{ label }}</slot>
    </span>
  </label>
</template>

<script setup>
import { computed, useSlots } from 'vue'

const props = defineProps({
  modelValue: {
    type: [Boolean, String, Number],
    default: false
  },
  label: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'large', 'small'].includes(val)
  },
  indeterminate: {
    type: Boolean,
    default: false
  },
  trueValue: {
    type: [Boolean, String, Number],
    default: true
  },
  falseValue: {
    type: [Boolean, String, Number],
    default: false
  },
  name: {
    type: String,
    default: ''
  },
  border: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

/* ---------- 计算属性 ---------- */

const isChecked = computed(() => {
  return props.modelValue === props.trueValue
})

const ariaChecked = computed(() => {
  if (props.indeterminate) return 'mixed'
  return isChecked.value
})

const slots = useSlots()

const hasLabel = computed(() => {
  return !!(props.label || slots.default)
})

const checkboxClasses = computed(() => {
  const classes = [`cc-size--${props.size}`]
  if (isChecked.value) classes.push('is-checked')
  if (props.indeterminate) classes.push('is-indeterminate')
  if (props.disabled) classes.push('is-disabled')
  if (props.border) classes.push('is-border')
  if (hasLabel.value) classes.push('has-label')
  return classes
})

const boxClasses = computed(() => {
  const classes = []
  if (isChecked.value) classes.push('is-checked')
  if (props.indeterminate) classes.push('is-indeterminate')
  return classes
})

/* ---------- 事件处理 ---------- */

function handleClick(event) {
  if (props.disabled) return

  const newChecked = !isChecked.value
  const newValue = newChecked ? props.trueValue : props.falseValue

  emit('update:modelValue', newValue)
  emit('change', newValue)
}
</script>

<style scoped lang="scss">
/* =========================================================
 * CustomCheckbox — 项目风格复选框组件
 * 替代 el-checkbox，完全可控的样式与交互
 * ========================================================= */

.custom-checkbox {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  user-select: none;
  font-family: var(--el-font-family);
  vertical-align: middle;
  outline: none;
  -webkit-tap-highlight-color: transparent;
  transition: opacity 0.3s ease;
}

/* ---------- 复选框本体 ---------- */
.cc-box {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background-color: transparent;
  box-shadow: var(--shadow-input);
  transition: all 0.3s ease;
  overflow: hidden;
}

/* 未选中 hover */
.custom-checkbox:hover:not(.is-disabled):not(.is-checked):not(.is-indeterminate) .cc-box {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-input-hover);
}

/* 选中状态 */
.cc-box.is-checked,
.cc-box.is-indeterminate {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: 0 2px 6px var(--primary-40);
}

/* 选中态 hover */
.custom-checkbox.is-checked:hover:not(.is-disabled) .cc-box,
.custom-checkbox.is-indeterminate:hover:not(.is-disabled) .cc-box {
  background-color: var(--primary-dark);
  border-color: var(--primary-dark);
  box-shadow: 0 3px 10px var(--primary-40);
}

/* ---------- 勾选图标 ---------- */
.cc-icon {
  color: var(--text-inverse);
  font-size: inherit;
  line-height: 1;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* ---------- 半选横线 ---------- */
.cc-indeterminate {
  display: block;
  background-color: var(--text-inverse);
  border-radius: 1px;
  transition: transform 0.25s ease;
}

/* ---------- 标签文本 ---------- */
.cc-label {
  font-size: 14px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
  line-height: 1.4;
}

.custom-checkbox.is-checked .cc-label {
  color: var(--text-primary);
}

/* ---------- 边框变体 ---------- */
.custom-checkbox.is-border {
  padding: 6px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background-color: var(--card-bg);
  box-shadow: var(--shadow-input);
  transition: all 0.3s ease;
}

.custom-checkbox.is-border:hover:not(.is-disabled):not(.is-checked):not(.is-indeterminate) {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-input-hover);
}

.custom-checkbox.is-border.is-checked,
.custom-checkbox.is-border.is-indeterminate {
  border-color: var(--primary-color);
  background-color: var(--primary-light);
  box-shadow: 0 2px 8px var(--primary-20);
}

.custom-checkbox.is-border.is-checked .cc-label,
.custom-checkbox.is-border.is-indeterminate .cc-label {
  color: var(--primary-dark);
  font-weight: 500;
}

/* ---------- 尺寸系统 ---------- */

/* default */
.cc-size--default .cc-box {
  width: 18px;
  height: 18px;
}

.cc-size--default .cc-icon {
  font-size: 12px;
}

.cc-size--default .cc-indeterminate {
  width: 10px;
  height: 2px;
}

.cc-size--default .cc-label {
  font-size: 14px;
}

/* large */
.cc-size--large .cc-box {
  width: 22px;
  height: 22px;
}

.cc-size--large .cc-icon {
  font-size: 14px;
}

.cc-size--large .cc-indeterminate {
  width: 12px;
  height: 2.5px;
}

.cc-size--large .cc-label {
  font-size: 16px;
}

.cc-size--large.is-border {
  padding: 8px 16px;
}

/* small */
.cc-size--small .cc-box {
  width: 14px;
  height: 14px;
}

.cc-size--small .cc-icon {
  font-size: 10px;
}

.cc-size--small .cc-indeterminate {
  width: 8px;
  height: 2px;
}

.cc-size--small .cc-label {
  font-size: 12px;
}

.cc-size--small.is-border {
  padding: 4px 10px;
}

/* ---------- 禁用状态 ---------- */
.custom-checkbox.is-disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.custom-checkbox.is-disabled .cc-box {
  box-shadow: none !important;
}

/* ---------- focus-visible 聚焦环 ---------- */
.custom-checkbox:focus-visible .cc-box {
  outline: none;
  box-shadow: 0 0 0 3px var(--primary-40);
}

.custom-checkbox.is-border:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--primary-40);
}

/* ---------- 勾选动画 ---------- */
.cc-check-enter-active,
.cc-check-leave-active {
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.cc-check-enter-from,
.cc-check-leave-to {
  opacity: 0;
  transform: scale(0.5);
}
</style>
