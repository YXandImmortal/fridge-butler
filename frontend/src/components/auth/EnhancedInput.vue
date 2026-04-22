<template>
  <el-input
      :model-value="modelValue"
      @update:model-value="$emit('update:modelValue', $event)"
      :placeholder="placeholder"
      class="enhanced-input"
      :type="type"
      :show-password="showPassword"
      :clearable="clearable"
      size="default"
  >
    <template #prefix>
      <i class="iconfont" :class="icon" />
    </template>
    <slot name="prefix"></slot>
    <slot name="suffix"></slot>
  </el-input>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    required: true
  },
  icon: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'text'
  },
  showPassword: {
    type: Boolean,
    default: false
  },
  clearable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue'])
</script>

<style scoped>
/* 输入框增强样式 */
.enhanced-input .iconfont {
  font-size: var(--space-5);
}

.enhanced-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-4);
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.enhanced-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border-color: rgba(0, 0, 0, 0.1);
}

.enhanced-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px var(--primary-30), 0 0 0 3px rgba(100, 181, 246, 0.15);
  border-color: var(--primary-color);
}
</style>