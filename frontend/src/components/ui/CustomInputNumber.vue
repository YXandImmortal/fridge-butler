<template>
  <div
    class="custom-input-number"
    :class="[
      sizeClass,
      { 'is-disabled': disabled, 'is-focused': isFocused, 'no-controls': !controls }
    ]"
    :style="containerStyle"
  >
    <!-- 左侧减号按钮 -->
    <button
      v-if="controls && controlsPosition === 'both'"
      type="button"
      class="cin-btn cin-btn--minus"
      :disabled="disabled || isMin"
      @mousedown.prevent="startDecrease"
      @mouseup="stopContinuous"
      @mouseleave="stopContinuous"
      @touchstart.prevent="startDecrease"
      @touchend="stopContinuous"
    >
      <span class="cin-icon cin-icon--minus"></span>
    </button>

    <!-- 输入框 -->
    <div class="cin-input-wrap">
      <input
        ref="inputRef"
        v-model="displayValue"
        type="text"
        inputmode="decimal"
        :placeholder="placeholder"
        :disabled="disabled"
        class="cin-input"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeydown"
      />
    </div>

    <!-- 右侧加号按钮（或 controlsPosition=right 时的上下按钮组） -->
    <template v-if="controls">
      <!-- 两侧模式：右侧只有加号 -->
      <button
        v-if="controlsPosition === 'both'"
        type="button"
        class="cin-btn cin-btn--plus"
        :disabled="disabled || isMax"
        @mousedown.prevent="startIncrease"
        @mouseup="stopContinuous"
        @mouseleave="stopContinuous"
        @touchstart.prevent="startIncrease"
        @touchend="stopContinuous"
      >
        <span class="cin-icon cin-icon--plus"></span>
      </button>

      <!-- 右侧模式：上下按钮 -->
      <div v-else class="cin-btn-group">
        <button
          type="button"
          class="cin-btn cin-btn--up"
          :disabled="disabled || isMax"
          @mousedown.prevent="startIncrease"
          @mouseup="stopContinuous"
          @mouseleave="stopContinuous"
          @touchstart.prevent="startIncrease"
          @touchend="stopContinuous"
        >
          <span class="cin-icon cin-icon--plus"></span>
        </button>
        <button
          type="button"
          class="cin-btn cin-btn--down"
          :disabled="disabled || isMin"
          @mousedown.prevent="startDecrease"
          @mouseup="stopContinuous"
          @mouseleave="stopContinuous"
          @touchstart.prevent="startDecrease"
          @touchend="stopContinuous"
        >
          <span class="cin-icon cin-icon--minus"></span>
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: null
  },
  min: {
    type: Number,
    default: -Infinity
  },
  max: {
    type: Number,
    default: Infinity
  },
  step: {
    type: Number,
    default: 1
  },
  precision: {
    type: Number,
    default: undefined
  },
  placeholder: {
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
  width: {
    type: String,
    default: ''
  },
  controls: {
    type: Boolean,
    default: true
  },
  controlsPosition: {
    type: String,
    default: 'both',
    validator: (val) => ['both', 'right'].includes(val)
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'focus', 'blur'])

const inputRef = ref(null)
const isFocused = ref(false)
const displayValue = ref('')
const continuousTimer = ref(null)
const continuousInterval = ref(null)

// ---------- 计算属性 ----------

const sizeClass = computed(() => `cin-size--${props.size}`)

const containerStyle = computed(() => {
  const style = {}
  if (props.width) style.width = props.width
  return style
})

const isMin = computed(() => {
  if (props.modelValue === null || props.modelValue === undefined) return false
  return props.modelValue <= props.min
})

const isMax = computed(() => {
  if (props.modelValue === null || props.modelValue === undefined) return false
  return props.modelValue >= props.max
})

// ---------- 核心数值处理 ----------

function toPrecision(num) {
  if (props.precision === undefined) return num
  const factor = Math.pow(10, props.precision)
  return Math.round(num * factor) / factor
}

function formatDisplay(val) {
  if (val === null || val === undefined || Number.isNaN(val)) return ''
  if (props.precision !== undefined) {
    return val.toFixed(props.precision)
  }
  return String(val)
}

function parseNumber(str) {
  if (!str && str !== '0') return null
  const num = parseFloat(str)
  if (Number.isNaN(num)) return null
  return num
}

function clampAndFormat(num) {
  if (num === null || num === undefined || Number.isNaN(num)) return null
  let val = num
  if (val < props.min) val = props.min
  if (val > props.max) val = props.max
  val = toPrecision(val)
  return val
}

// ---------- 对外值同步 ----------

watch(
  () => props.modelValue,
  (val) => {
    displayValue.value = formatDisplay(val)
  },
  { immediate: true }
)

function updateValue(num, shouldEmitChange = true) {
  const oldVal = props.modelValue
  const newVal = clampAndFormat(num)
  if (newVal !== oldVal) {
    emit('update:modelValue', newVal)
    if (shouldEmitChange) {
      emit('change', newVal, oldVal)
    }
  }
  // 同步显示值（处理如 01 -> 1, 1.999 -> 2.00 等）
  displayValue.value = formatDisplay(newVal)
}

// ---------- 按钮操作 ----------

function increase() {
  const current = props.modelValue !== null ? props.modelValue : 0
  updateValue(current + props.step)
}

function decrease() {
  const current = props.modelValue !== null ? props.modelValue : 0
  updateValue(current - props.step)
}

function startIncrease() {
  if (props.disabled || isMax.value) return
  increase()
  // 长按加速
  continuousTimer.value = setTimeout(() => {
    continuousInterval.value = setInterval(() => {
      if (!isMax.value) increase()
    }, 80)
  }, 400)
}

function startDecrease() {
  if (props.disabled || isMin.value) return
  decrease()
  continuousTimer.value = setTimeout(() => {
    continuousInterval.value = setInterval(() => {
      if (!isMin.value) decrease()
    }, 80)
  }, 400)
}

function stopContinuous() {
  if (continuousTimer.value) {
    clearTimeout(continuousTimer.value)
    continuousTimer.value = null
  }
  if (continuousInterval.value) {
    clearInterval(continuousInterval.value)
    continuousInterval.value = null
  }
}

// ---------- 输入事件 ----------

function handleFocus() {
  isFocused.value = true
  emit('focus')
}

function handleBlur() {
  isFocused.value = false
  const num = parseNumber(displayValue.value)
  updateValue(num)
  emit('blur')
}

function handleKeydown(e) {
  if (props.disabled) return
  switch (e.key) {
    case 'ArrowUp':
      e.preventDefault()
      increase()
      break
    case 'ArrowDown':
      e.preventDefault()
      decrease()
      break
    case 'Enter':
      inputRef.value?.blur()
      break
  }
}
</script>

<style scoped lang="scss">
/* =========================================================
 * CustomInputNumber — 项目风格数字输入组件
 * 替代 el-input-number，完全可控的样式与交互
 * ========================================================= */

.custom-input-number {
  display: inline-flex;
  align-items: stretch;
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-input);
  transition: all 0.3s ease;
  overflow: hidden;
  font-family: var(--el-font-family);
  vertical-align: middle;
  width: 180px;
}

.custom-input-number:hover:not(.is-disabled) {
  box-shadow: var(--shadow-input-hover);
  border-color: var(--border-color);
}

.custom-input-number.is-focused:not(.is-disabled) {
  box-shadow: var(--shadow-input-focus);
  border-color: var(--primary-color);
}

.custom-input-number.is-disabled {
  opacity: 0.65;
  cursor: not-allowed;
  background-color: var(--disabled-bg);
  border: var(--disabled-border);
  box-shadow: none;
}

/* ---------- 输入框区域 ---------- */
.cin-input-wrap {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  align-items: center;
}

.cin-input {
  width: 100%;
  height: 100%;
  padding: 0 var(--space-3);
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  font-family: inherit;
  font-size: 14px;
  line-height: 1;
  text-align: center;

  /* 移除浏览器 number 输入的默认箭头 */
  &::-webkit-outer-spin-button,
  &::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
  &[type='number'] {
    -moz-appearance: textfield;
  }

  &::placeholder {
    color: var(--text-tertiary);
    font-weight: 200;
  }

  &:disabled {
    cursor: not-allowed;
    color: var(--disabled-text);
  }
}

/* ---------- 按钮 ---------- */
.cin-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  padding: 0;
  flex-shrink: 0;
}

.cin-btn:hover:not(:disabled) {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.cin-btn:active:not(:disabled) {
  background: var(--primary-color);
  color: var(--text-inverse);
}

.cin-btn:disabled {
  cursor: not-allowed;
  opacity: 0.35;
}

/* 图标：使用纯 CSS 绘制 + / -，确保任意主题下清晰锐利 */
.cin-icon {
  position: relative;
  display: inline-block;
}

.cin-icon--minus::before {
  content: '';
  display: block;
  width: 10px;
  height: 2px;
  background-color: currentColor;
  border-radius: 1px;
}

.cin-icon--plus::before,
.cin-icon--plus::after {
  content: '';
  display: block;
  position: absolute;
  background-color: currentColor;
  border-radius: 1px;
}

.cin-icon--plus::before {
  width: 10px;
  height: 2px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.cin-icon--plus::after {
  width: 2px;
  height: 10px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* ---------- 按钮分组（controlsPosition=right） ---------- */
.cin-btn-group {
  display: flex;
  flex-direction: column;
  width: 32px;
  border-left: 1px solid var(--border-color);
}

.cin-btn-group .cin-btn {
  flex: 1;
}

.cin-btn-group .cin-btn--up {
  border-bottom: 1px solid var(--border-color);
}

/* ---------- 尺寸系统 ---------- */

/* default */
.cin-size--default {
  height: 38px;
}
.cin-size--default .cin-btn {
  width: 32px;
}
.cin-size--default .cin-input {
  font-size: 14px;
}

/* large */
.cin-size--large {
  height: 42px;
}
.cin-size--large .cin-btn {
  width: 36px;
}
.cin-size--large .cin-input {
  font-size: 16px;
}
.cin-size--large .cin-btn-group {
  width: 36px;
}

/* small */
.cin-size--small {
  height: 28px;
}
.cin-size--small .cin-btn {
  width: 24px;
}
.cin-size--small .cin-input {
  font-size: 12px;
}
.cin-size--small .cin-btn-group {
  width: 24px;
}
.cin-size--small .cin-icon--minus::before {
  width: 8px;
}
.cin-size--small .cin-icon--plus::before {
  width: 8px;
}
.cin-size--small .cin-icon--plus::after {
  height: 8px;
}

/* ---------- 无边框按钮分隔（两侧模式时中间不要额外边框） ---------- */
.cin-btn--minus {
  border-right: 1px solid var(--border-color);
}
.cin-btn--plus {
  border-left: 1px solid var(--border-color);
}

/* disabled 状态下的边框微调 */
.custom-input-number.is-disabled .cin-btn--minus,
.custom-input-number.is-disabled .cin-btn--plus,
.custom-input-number.is-disabled .cin-btn-group {
  border-color: transparent;
}

/* 无按钮模式 */
.custom-input-number.no-controls .cin-input {
  text-align: left;
  padding: 0 var(--space-4);
}
</style>
