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
  padding: 10px var(--space-4);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.enhanced-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px var(--primary-20);
}

.enhanced-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px var(--primary-30);
}
</style>