<template>
  <el-form-item class="button-group">
    <el-button
        type="primary"
        @click="primaryAction"
        class="enhanced-button auth-primary-btn"
        :loading="loading"
    >
      {{ loading ? loadingText : primaryText }}
    </el-button>
    <el-button
        @click="secondaryAction"
        class="enhanced-button auth-secondary-btn"
    >
      {{ secondaryText }}
    </el-button>
  </el-form-item>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  primaryText: {
    type: String,
    required: true
  },
  secondaryText: {
    type: String,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: '处理中...'
  }
})

const emit = defineEmits(['primaryAction', 'secondaryAction'])

const primaryAction = () => {
  emit('primaryAction')
}

const secondaryAction = () => {
  emit('secondaryAction')
}
</script>

<style scoped>
@import '@/assets/theme.css';
</style>