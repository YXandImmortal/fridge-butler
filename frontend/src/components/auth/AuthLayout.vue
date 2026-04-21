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
/* 认证页面容器 */
.auth-page {
    min-height: 100vh;
    background-image: url("@/assets/images/auth-bg.png");
    background-repeat: no-repeat;
    background-size: cover;
  background-position: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
}

/* 认证页面装饰元素 */
.auth-page::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, var(--white-10) 0%, rgba(255,255,255,0) 70%);
    animation: pulse 8s ease-in-out infinite;
}

/* 认证页面包装器 */
.auth-wrapper {
    width: 100%;
    max-width: 480px;
    animation: fade-in-up 0.6s ease-out;
    transform: translateZ(0);
    position: relative;
    z-index: 1;
}

/* 认证卡片 */
.auth-card {
    padding: 0 40px 20px 40px;
    transition: all 0.3s ease;
}

.auth-card:hover {
    transform: translateY(-4px);
}

/* 响应式设计 */
@media (max-width: 480px) {
    .auth-card {
        padding: 30px 20px;
    }
}
</style>