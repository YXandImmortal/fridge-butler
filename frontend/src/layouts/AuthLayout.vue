<template>
  <div class="auth-page">
    <div class="auth-back-home" @click="goToHome">
      <i class="iconfont icon-home"/>
      <span>返回首页</span>
    </div>
    <div class="auth-theme-toggle">
      <ThemeToggle/>
    </div>
    <div class="auth-wrapper">
      <el-card class="auth-card glass-card">
        <AuthHeader
            :title="title"
            :subtitle="subtitle"
        />
        <slot></slot>
      </el-card>
    </div>
    <CopyrightFooter/>
  </div>
</template>

<script setup>
import {defineProps} from 'vue'
import {useRouter} from 'vue-router'
import AuthHeader from '../components/auth/AuthHeader.vue'
import CopyrightFooter from '../components/layout/CopyrightFooter.vue'
import ThemeToggle from '../components/ui/ThemeToggle.vue'

const router = useRouter()
const goToHome = () => router.push('/')

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

<style scoped lang="scss">
/* 返回首页按钮 */
.auth-back-home {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  border: 1px solid var(--border-light);
  border-radius: 20px;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.auth-back-home:hover {
  transform: translateY(-2px);
  border-color: var(--primary-color);
  box-shadow: 0 4px 16px var(--shadow-color);
  color: var(--primary-color);
}

.auth-back-home .iconfont {
  font-size: 16px;
}

/* 主题切换按钮定位 */
.auth-theme-toggle {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 10;
}

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
  background: radial-gradient(circle, var(--white-10) 0%, transparent 70%);
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