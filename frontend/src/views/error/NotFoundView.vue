<template>
  <div class="not-found-page">
    <div class="snowflakes">
      <span v-for="i in 12" :key="i" class="snowflake" />
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
          <!-- 内部空的状态 — 显示一点冷气 -->
          <circle cx="70" cy="80" r="4" fill="var(--color-primary-100)" opacity="0.6">
            <animate attributeName="cy" values="80;70;80" dur="3s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="0.6;0;0.6" dur="3s" repeatCount="indefinite" />
          </circle>
          <circle cx="100" cy="90" r="3" fill="var(--color-primary-100)" opacity="0.5">
            <animate attributeName="cy" values="90;75;90" dur="2.5s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="0.5;0;0.5" dur="2.5s" repeatCount="indefinite" />
          </circle>
          <circle cx="55" cy="170" r="3.5" fill="var(--color-primary-100)" opacity="0.4">
            <animate attributeName="cy" values="170;155;170" dur="2.8s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="0.4;0;0.4" dur="2.8s" repeatCount="indefinite" />
          </circle>
          <!-- 问号 -->
          <text x="95" y="210" font-size="36" fill="var(--color-primary-300)" font-family="MiSans, sans-serif" font-weight="bold" text-anchor="middle">?</text>
        </svg>
      </div>

      <!-- 404 数字 -->
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
        <span class="digit">4</span>
      </h1>

      <!-- 文案 -->
      <h2 class="error-title">页面像这个冰箱一样 — 空空如也</h2>
      <p class="error-desc">
        您访问的页面不存在，可能已经被“冷藏”起来了
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
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-page);
  position: relative;
  overflow: hidden;

}

/* 雪花背景 */
.snowflakes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.snowflake {
  position: absolute;
  top: -20px;
  width: 8px;
  height: 8px;
  background: var(--primary-light);
  border-radius: 50%;
  opacity: 0.5;
  animation: snowfall linear infinite;
}

.snowflake:nth-child(1) { left: 10%; animation-duration: 8s; animation-delay: 0s; width: 6px; height: 6px; }
.snowflake:nth-child(2) { left: 20%; animation-duration: 10s; animation-delay: 1s; width: 10px; height: 10px; }
.snowflake:nth-child(3) { left: 30%; animation-duration: 7s; animation-delay: 2s; }
.snowflake:nth-child(4) { left: 40%; animation-duration: 9s; animation-delay: 0.5s; width: 5px; height: 5px; }
.snowflake:nth-child(5) { left: 50%; animation-duration: 11s; animation-delay: 3s; width: 7px; height: 7px; }
.snowflake:nth-child(6) { left: 60%; animation-duration: 8.5s; animation-delay: 1.5s; width: 9px; height: 9px; }
.snowflake:nth-child(7) { left: 70%; animation-duration: 9.5s; animation-delay: 2.5s; }
.snowflake:nth-child(8) { left: 80%; animation-duration: 7.5s; animation-delay: 0.2s; width: 6px; height: 6px; }
.snowflake:nth-child(9) { left: 90%; animation-duration: 10.5s; animation-delay: 1.2s; width: 8px; height: 8px; }
.snowflake:nth-child(10) { left: 15%; animation-duration: 8s; animation-delay: 3.5s; width: 5px; height: 5px; }
.snowflake:nth-child(11) { left: 55%; animation-duration: 9s; animation-delay: 4s; width: 7px; height: 7px; }
.snowflake:nth-child(12) { left: 85%; animation-duration: 11s; animation-delay: 2.2s; width: 6px; height: 6px; }

@keyframes snowfall {
  0% {
    transform: translateY(-20px) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.5;
  }
  90% {
    opacity: 0.5;
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
  filter: drop-shadow(0 8px 24px rgba(100, 181, 246, 0.2));
}

/* 404 数字 */
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
  background: linear-gradient(135deg, var(--color-primary-400), var(--success-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.fridge-o {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary-400);
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
  background: linear-gradient(90deg, var(--primary-color), var(--success-color));
  border: none;
  font-weight: 500;
  padding: 0 var(--space-8);
  height: 44px;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--primary-30);
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
