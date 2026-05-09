<template>
  <div class="error-page">
    <div class="ice-particles">
      <span v-for="i in 10" :key="i" class="ice-particle" />
    </div>

    <div class="content-wrapper">
      <!-- 冰箱插画 -->
      <div class="fridge-illustration">
        <svg viewBox="0 0 200 280" class="fridge-svg">
          <!-- 冰箱主体 -->
          <rect x="30" y="20" width="140" height="240" rx="12" fill="var(--color-primary-50)" stroke="var(--color-primary-300)" stroke-width="3" />
          <!-- 上门 -->
          <rect x="30" y="20" width="140" height="100" rx="12" fill="var(--color-primary-50)" stroke="var(--color-primary-300)" stroke-width="2" />
          <line x1="30" y1="120" x2="170" y2="120" stroke="var(--color-primary-300)" stroke-width="2" />
          <!-- 下门 -->
          <rect x="30" y="120" width="140" height="140" rx="12" fill="var(--color-primary-50)" stroke="var(--color-primary-300)" stroke-width="2" />
          <!-- 把手 -->
          <rect x="145" y="55" width="8" height="30" rx="4" fill="var(--color-primary-200)" />
          <rect x="145" y="155" width="8" height="30" rx="4" fill="var(--color-primary-200)" />
          <!-- 锁图标 -->
          <circle cx="100" cy="140" r="28" fill="var(--warn-color)" opacity="0.15" />
          <rect x="85" y="145" width="30" height="24" rx="4" fill="none" stroke="var(--warn-color)" stroke-width="3" />
          <path d="M92 145 L92 138 A8 8 0 0 1 108 138 L108 145" fill="none" stroke="var(--warn-color)" stroke-width="3" stroke-linecap="round" />
          <circle cx="100" cy="156" r="3" fill="var(--warn-color)" />
          <line x1="100" y1="156" x2="100" y2="162" stroke="var(--warn-color)" stroke-width="2" stroke-linecap="round" />
        </svg>
      </div>

      <!-- 403 数字 -->
      <h1 class="error-code">
        <span class="digit">4</span>
        <span class="digit fridge-o">
          <svg viewBox="0 0 60 80" class="mini-fridge">
            <rect x="8" y="4" width="44" height="72" rx="6" fill="none" stroke="currentColor" stroke-width="4" />
            <line x1="8" y1="30" x2="52" y2="30" stroke="currentColor" stroke-width="3" />
            <rect x="42" y="14" width="4" height="12" rx="2" fill="currentColor" />
            <rect x="42" y="40" width="4" height="12" rx="2" fill="currentColor" />
          </svg>
        </span>
        <span class="digit">3</span>
      </h1>

      <!-- 文案 -->
      <h2 class="error-title">这扇冰箱门被锁住了 — 您没有权限打开</h2>
      <p class="error-desc">
        您尝试访问的资源需要更高的权限等级，请联系管理员获取权限
      </p>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button type="primary" size="large" class="home-btn" @click="goHome">
          <i class="iconfont icon-home" />
          返回首页
        </el-button>
        <el-button size="large" class="back-btn" @click="goBack">
          <i class="iconfont icon-arrow-left" />
          上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/user/index')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-page);
  position: relative;
  overflow: hidden;
}

/* 冰晶背景 */
.ice-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.ice-particle {
  position: absolute;
  top: -20px;
  width: 6px;
  height: 6px;
  background: var(--warn-color);
  border-radius: 50%;
  opacity: 0.3;
  animation: ice-fall linear infinite;
}

.ice-particle:nth-child(1) { left: 10%; animation-duration: 7s; animation-delay: 0s; width: 4px; height: 4px; }
.ice-particle:nth-child(2) { left: 25%; animation-duration: 9s; animation-delay: 1s; width: 8px; height: 8px; }
.ice-particle:nth-child(3) { left: 35%; animation-duration: 6s; animation-delay: 2s; }
.ice-particle:nth-child(4) { left: 45%; animation-duration: 8s; animation-delay: 0.5s; width: 5px; height: 5px; }
.ice-particle:nth-child(5) { left: 55%; animation-duration: 10s; animation-delay: 3s; width: 6px; height: 6px; }
.ice-particle:nth-child(6) { left: 65%; animation-duration: 7.5s; animation-delay: 1.5s; width: 7px; height: 7px; }
.ice-particle:nth-child(7) { left: 75%; animation-duration: 8.5s; animation-delay: 2.5s; }
.ice-particle:nth-child(8) { left: 85%; animation-duration: 6.5s; animation-delay: 0.2s; width: 5px; height: 5px; }
.ice-particle:nth-child(9) { left: 15%; animation-duration: 9.5s; animation-delay: 1.2s; width: 6px; height: 6px; }
.ice-particle:nth-child(10) { left: 50%; animation-duration: 8s; animation-delay: 3.5s; width: 4px; height: 4px; }

@keyframes ice-fall {
  0% {
    transform: translateY(-20px) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.3;
  }
  90% {
    opacity: 0.3;
  }
  100% {
    transform: translateY(100vh) rotate(360deg);
    opacity: 0;
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
  filter: drop-shadow(0 8px 24px rgba(255, 183, 77, 0.2));
}

/* 403 数字 */
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
  background: linear-gradient(135deg, var(--warn-color), var(--color-orange-500));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.fridge-o {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--warn-color);
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
  background: linear-gradient(90deg, var(--warn-color), var(--color-orange-500));
  border: none;
  font-weight: 500;
  padding: 0 var(--space-8);
  height: 44px;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 183, 77, 0.3);
}

.back-btn {
  font-weight: 500;
  padding: 0 var(--space-8);
  height: 44px;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.back-btn:hover {
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
