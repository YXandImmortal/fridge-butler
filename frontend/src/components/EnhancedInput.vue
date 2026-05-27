<template>
  <el-input
      :model-value="modelValue"
      @update:model-value="$emit('update:modelValue', $event)"
      :placeholder="placeholder"
      :class="['enhanced-input', variantClass]"
      :style="inputStyle"
      :type="type"
      :show-password="showPassword"
      :clearable="clearable"
      :disabled="disabled"
      :maxlength="maxlength"
      :show-word-limit="showWordLimit"
      :rows="rows"
      size="default"
  >
    <template v-if="icon" #prefix>
      <i class="iconfont" :class="icon"/>
    </template>
    <slot name="prefix"/>
    <slot name="suffix"/>
  </el-input>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
  modelValue: {type: String, default: ''},
  placeholder: {type: String, default: ''},
  icon: {type: String, default: ''},
  type: {type: String, default: 'text'},
  showPassword: {type: Boolean, default: false},
  clearable: {type: Boolean, default: false},
  disabled: {type: Boolean, default: false},
  width: {type: String, default: ''},
  height: {type: String, default: ''},
  variant: {type: String, default: 'default'}, // 'default' | 'search'
  maxlength: {type: [String, Number], default: undefined},
  showWordLimit: {type: Boolean, default: false},
  rows: {type: [String, Number], default: 2}
})

const emit = defineEmits(['update:modelValue'])

const variantClass = computed(() => {
  if (props.type === 'textarea') return 'enhanced-textarea'
  if (props.variant === 'search') return 'enhanced-search'
  return 'enhanced-default'
})

const inputStyle = computed(() => {
  const style = {}
  if (props.width) style.width = props.width
  if (props.height) style['--enhanced-input-height'] = props.height
  return style
})
</script>

<style scoped lang="scss">
/* 基础容器 */
.enhanced-input {
  width: 100%;
}

/* ========== 默认输入框 ========== */
.enhanced-default :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-4);
  background-color: var(--card-bg);
  box-shadow: var(--shadow-input);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

/* 方便的高度调节 */
.enhanced-default :deep(.el-input__inner) {
  height: var(--enhanced-input-height, 32px);
}

/* 统一的 hover 样式 */
.enhanced-default :deep(.el-input__wrapper:hover) {
  box-shadow: var(--shadow-input-hover);
  border-color: var(--border-color);
}

/* 统一的 focus 样式 */
.enhanced-default :deep(.el-input__wrapper.is-focus) {
  box-shadow: var(--shadow-input-focus);
  border-color: var(--primary-color);
}

/* ========== 搜索输入框（SearchBar 专用） ========== */
.enhanced-search :deep(.el-input__wrapper) {
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  border-right: none;
  box-shadow: 0 0 0 1px var(--gray-40) inset;
  padding: var(--space-1) var(--space-4);
  background-color: var(--card-bg);
  transition: all 0.3s ease;
}

.enhanced-search :deep(.el-input__inner) {
  height: var(--enhanced-input-height, 40px);
  font-size: 16px;
}

.enhanced-search :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--gray-40) inset;
}

.enhanced-search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
  border-right: none;
}

/* ========== 文本域 ========== */
.enhanced-textarea :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  padding: 12px var(--space-4);
  background-color: var(--card-bg);
  box-shadow: var(--shadow-input);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  resize: none;
}

.enhanced-textarea :deep(.el-textarea__inner:hover) {
  box-shadow: var(--shadow-input-hover);
  border-color: var(--border-color);
}

.enhanced-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: 0 4px 16px var(--primary-30), 0 0 0 3px rgba(100, 181, 246, 0.15);
  border-color: var(--primary-color);
}

/* ========== 统一的禁用样式 ========== */
.enhanced-input.is-disabled :deep(.el-input__wrapper) {
  background-color: var(--disabled-bg) !important;
  border: var(--disabled-border) !important;
  box-shadow: none !important;
  cursor: default;
}

.enhanced-input.is-disabled :deep(.el-input__wrapper:hover) {
  box-shadow: none !important;
  border-color: var(--primary-color) !important;
}

.enhanced-input.is-disabled :deep(.el-input__inner) {
  color: var(--text-secondary);
  font-weight: 500;
  cursor: default;
}

/* 图标样式 */
.enhanced-input .iconfont {
  font-size: var(--space-5);
}
</style>
