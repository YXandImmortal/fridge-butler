<template>
  <el-input
      :model-value="modelValue"
      @update:model-value="$emit('update:modelValue', $event)"
      :placeholder="placeholder"
      class="enhanced-input"
      :type="type"
      :show-password="showPassword"
      :clearable="clearable"
      size="large"
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
@import '@/assets/theme.css';
</style>