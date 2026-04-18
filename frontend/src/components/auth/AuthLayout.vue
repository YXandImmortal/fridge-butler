<template>
  <div class="auth-page">
    <div class="auth-wrapper">
      <el-card class="auth-card glass-card">
        <AuthHeader
            :title="title"
            :subtitle="subtitle"
        />
        <slot></slot>
      </el-card>
    </div>
    <CopyrightFooter />
  </div>
</template>

<script setup>
import { defineProps } from 'vue'
import AuthHeader from './AuthHeader.vue'
import CopyrightFooter from '../CopyrightFooter.vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
@import '@/assets/theme.css';
</style>