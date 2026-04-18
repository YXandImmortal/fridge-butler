<template>
  <div class="auth-header">
    <div class="icon-container">
      <Logo />
    </div>
    <h2 class="auth-title animated-title">{{ title }}</h2>
    <p class="auth-subtitle">{{ subtitle }}</p>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'
import Logo from '@/components/Logo.vue'

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