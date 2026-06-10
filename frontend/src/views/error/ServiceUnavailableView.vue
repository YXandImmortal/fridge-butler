<template>
  <div class="error-page">
    <div class="maintenance-icons">
      <span v-for="i in 8" :key="i" class="floating-icon"/>
    </div>

    <div class="content-wrapper">
      <!-- 冰箱插画 -->
      <div class="fridge-illustration">
        <svg viewBox="0 0 200 280" class="fridge-svg">
          <!-- 冰箱主体 -->
          <rect x="30" y="20" width="140" height="240" rx="12" fill="var(--color-primary-50)"
                stroke="var(--color-primary-300)" stroke-width="3"/>
          <!-- 上门 -->
          <rect x="30" y="20" width="140" height="100" rx="12" fill="var(--color-primary-50)"
                stroke="var(--color-primary-300)" stroke-width="2"/>
          <line x1="30" y1="120" x2="170" y2="120" stroke="var(--color-primary-300)" stroke-width="2"/>
          <!-- 下门 -->
          <rect x="30" y="120" width="140" height="140" rx="12" fill="var(--color-primary-50)"
                stroke="var(--color-primary-300)" stroke-width="2"/>
          <!-- 把手 -->
          <rect x="145" y="55" width="8" height="30" rx="4" fill="var(--color-primary-200)"/>
          <rect x="145" y="155" width="8" height="30" rx="4" fill="var(--color-primary-200)"/>
          <!-- 维护标签 -->
          <rect x="50" y="80" width="100" height="36" rx="6" fill="var(--color-purple-400)" opacity="0.15"/>
          <rect x="53" y="83" width="94" height="30" rx="4" fill="none" stroke="var(--color-purple-500)"
                stroke-width="1.5" stroke-dasharray="4 2"/>
          <text x="100" y="104" font-size="11" fill="var(--color-purple-500)" font-family="MiSans, sans-serif"
                font-weight="bold" text-anchor="middle">维护中
          </text>
          <!-- 扳手图标 -->
          <g transform="translate(85, 155)">
            <rect x="12" y="0" width="6" height="20" rx="2" fill="var(--color-purple-400)"
                  transform="rotate(45 15 10)"/>
            <circle cx="22" cy="16" r="5" fill="none" stroke="var(--color-purple-400)" stroke-width="2.5"/>
            <circle cx="8" cy="4" r="5" fill="none" stroke="var(--color-purple-400)" stroke-width="2.5"/>
            <animateTransform attributeName="transform" type="rotate"
                              values="0 100 155; 10 100 155; 0 100 155; -10 100 155; 0 100 155" dur="4s"
                              repeatCount="indefinite"/>
          </g>
          <!-- 进度条 -->
          <rect x="55" y="220" width="90" height="6" rx="3" fill="var(--color-primary-100)"/>
          <rect x="55" y="220" width="60" height="6" rx="3" fill="var(--color-purple-400)">
            <animate attributeName="width" values="0;90;60;80;60" dur="4s" repeatCount="indefinite"/>
          </rect>
        </svg>
      </div>

      <!-- 503 数字 -->
      <h1 class="error-code">
        <span class="digit">5</span>
        <span class="digit fridge-o">
          <svg viewBox="0 0 60 80" class="mini-fridge">
            <rect x="8" y="4" width="44" height="72" rx="6" fill="none" stroke="currentColor" stroke-width="4"/>
            <line x1="8" y1="30" x2="52" y2="30" stroke="currentColor" stroke-width="3"/>
            <rect x="42" y="14" width="4" height="12" rx="2" fill="currentColor"/>
            <rect x="42" y="40" width="4" height="12" rx="2" fill="currentColor"/>
          </svg>
        </span>
        <span class="digit">3</span>
      </h1>

      <!-- 文案 -->
      <h2 class="error-title">冰箱正在除霜维护中 — 请稍后再来</h2>
      <p class="error-desc">
        服务暂时不可用，我们正在努力恢复中，预计很快完成
      </p>

      <!-- 操作按钮 -->
      <div class="actions">
        <CustomButton type="primary" size="large" class="home-btn" @click="goHome">
          <i class="iconfont icon-home"/>
          返回首页
        </CustomButton>
        <CustomButton size="large" class="back-btn" @click="goBack">
          <i class="iconfont icon-arrow-left"/>
          上一页
        </CustomButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const goHome = () => {
  if (!userStore.isLoggedIn) {
    router.push({name: 'landing'})
  } else if (userStore.roleId === 1) {
    router.push({name: 'super-admin-dashboard'})
  } else {
    router.push({name: 'user-index'})
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-page);
  position: relative;
  overflow: hidden;
}

/* 浮动装饰背景 */
.maintenance-icons {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.floating-icon {
  position: absolute;
  width: 10px;
  height: 10px;
  background: var(--color-purple-400);
  border-radius: 2px;
  opacity: 0.2;
  animation: float ease-in-out infinite;
}

.floating-icon:nth-child(1) {
  left: 10%;
  top: 20%;
  animation-duration: 6s;
  animation-delay: 0s;
  width: 8px;
  height: 8px;
}

.floating-icon:nth-child(2) {
  left: 20%;
  top: 70%;
  animation-duration: 8s;
  animation-delay: 1s;
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.floating-icon:nth-child(3) {
  left: 35%;
  top: 15%;
  animation-duration: 7s;
  animation-delay: 2s;
  width: 6px;
  height: 6px;
}

.floating-icon:nth-child(4) {
  left: 50%;
  top: 80%;
  animation-duration: 9s;
  animation-delay: 0.5s;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.floating-icon:nth-child(5) {
  left: 65%;
  top: 25%;
  animation-duration: 7.5s;
  animation-delay: 3s;
  width: 8px;
  height: 8px;
}

.floating-icon:nth-child(6) {
  left: 75%;
  top: 65%;
  animation-duration: 8.5s;
  animation-delay: 1.5s;
  width: 11px;
  height: 11px;
  border-radius: 50%;
}

.floating-icon:nth-child(7) {
  left: 85%;
  top: 30%;
  animation-duration: 6.5s;
  animation-delay: 2.5s;
  width: 7px;
  height: 7px;
}

.floating-icon:nth-child(8) {
  left: 45%;
  top: 45%;
  animation-duration: 10s;
  animation-delay: 0.8s;
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.2;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.4;
  }
}

/* 内容区 */
.content-wrapper {
  text-align: center;
  z-index: 1;
  padding: var(--space-8);
  animation: fade-in-up 0.6s ease-out;
}

/* 冰箱插画 */
.fridge-illustration {
  margin-bottom: var(--space-6);
}

.fridge-svg {
  width: 160px;
  height: 224px;
  filter: drop-shadow(0 8px 24px rgba(179, 157, 219, 0.2));
}

/* 503 数字 */
.error-code {
  font-size: 120px;
  font-weight: 800;
  line-height: 1;
  margin-bottom: var(--space-6);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
}

.digit {
  background: linear-gradient(135deg, var(--color-purple-400), var(--color-purple-500));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.fridge-o {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-purple-400);
}

.mini-fridge {
  width: 60px;
  height: 80px;
}

/* 标题与描述 */
.error-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-3);
}

.error-desc {
  font-size: 16px;
  color: var(--text-tertiary);
  margin-bottom: var(--space-8);
}

/* 按钮 */
.actions {
  display: flex;
  gap: var(--space-4);
  justify-content: center;
}

.home-btn {
  background: linear-gradient(90deg, var(--color-purple-400), var(--color-purple-500));
  border: none;
  font-weight: 500;
  padding: 0 var(--space-8);
  height: 44px;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.home-btn:hover:not(:disabled) {
  background: linear-gradient(90deg, var(--color-purple-400), var(--color-purple-500));
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(179, 157, 219, 0.3);
}

.back-btn {
  font-weight: 500;
  padding: 0 var(--space-8);
  height: 44px;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.back-btn:hover:not(:disabled) {
  background: transparent;
  border-color: var(--gray-40);
  color: var(--text-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* 响应式 */
@media (max-width: 480px) {
  .error-code {
    font-size: 80px;
  }

  .mini-fridge {
    width: 44px;
    height: 58px;
  }

  .fridge-svg {
    width: 120px;
    height: 168px;
  }

  .error-title {
    font-size: 20px;
  }

  .actions {
    flex-direction: column;
    align-items: center;
  }

  .home-btn,
  .back-btn {
    width: 200px;
  }
}
</style>
