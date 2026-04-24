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
/* 认证头部 */
.auth-header {
    text-align: center;
    margin-bottom: 30px;
}

/* 认证标题 */
.auth-title {
    font-size: 28px;
    font-weight: 700;
    background-color: var(--text-primary);
    margin: 4px 0 8px 0;
    animation: fade-in-down 0.6s ease-out;
    transform: translateZ(0);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

/* 认证副标题 */
.auth-subtitle {
    font-size: 14px;
    color: var(--auth-subtitle-color);
    margin: 0;
}

/* 图标容器 */
.icon-container {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 120px;
    height: 120px;
    background-color: rgba(0, 0, 0, 0);
    transition: all 0.3s ease;
}

.icon-container:hover {
    transform: scale(1.1);
}

.icon-container svg {
    width: 40px;
    height: 40px;
    fill: var(--text-inverse);
}

/* 响应式设计 */
@media (max-width: 480px) {
    .auth-title {
        font-size: 24px;
    }
}
</style>