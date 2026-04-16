<template>
  <div class="auth-header">
    <div class="icon-container">
      <svg v-if="icon === 'login'" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 18H6V4h12v16z"/>
        <path d="M8 6h8v2H8zm0 4h8v2H8zm0 4h5v2H8z"/>
      </svg>
      <svg v-else-if="icon === 'register'" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
      </svg>
    </div>
    <h2 class="auth-title animated-title">{{ title }}</h2>
    <p class="auth-subtitle">{{ subtitle }}</p>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    required: true
  },
  icon: {
    type: String,
    required: true,
    validator: (value) => ['login', 'register'].includes(value)
  }
})
</script>

<style scoped>
@import '@/assets/theme.css';
</style>