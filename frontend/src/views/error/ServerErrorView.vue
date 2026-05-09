<template>
  <div class="error-page">
    <div class="sparks">
      <span v-for="i in 12" :key="i" class="spark" />
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
          <!-- 故障指示灯 -->
          <circle cx="55" cy="50" r="6" fill="var(--danger-color)">
            <animate attributeName="opacity" values="1;0.3;1" dur="1s" repeatCount="indefinite" />
          </circle>
          <!-- 错误代码显示 -->
          <rect x="70" y="42" width="60" height="16" rx="3" fill="var(--danger-color)" opacity="0.15" />
          <text x="100" y="54" font-size="10" fill="var(--danger-color)" font-family="MiSans, sans-serif" font-weight="bold" text-anchor="middle">ERROR</text>
          <!-- 蒸汽/烟雾 -->
          <path d="M60 180 Q55 170 60 160 Q65 150 60 140" fill="none" stroke="var(--text-tertiary)" stroke-width="2" opacity="0.4">
            <animate attributeName="d" values="M60 180 Q55 170 60 160 Q65 150 60 140;M60 175 Q65 165 60 155 Q55 145 60 135;M60 180 Q55 170 60 160 Q65 150 60 140" dur="3s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="0.4;0.1;0.4" dur="3s" repeatCount="indefinite" />
          </path>
          <path d="M100 185 Q95 175 100 165 Q105 155 100 145" fill="none" stroke="var(--text-tertiary)" stroke-width="2" opacity="0.3">
            <animate attributeName="d" values="M100 185 Q95 175 100 165 Q105 155 100 145;M100 180 Q105 170 100 160 Q95 150 100 140;M100 185 Q95 175 100 165 Q105 155 100 145" dur="2.5s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="0.3;0;0.3" dur="2.5s" repeatCount="indefinite" />
          </path>
          <path d="M140 180 Q135 170 140 160 Q145 150 140 140" fill="none" stroke="var(--text-tertiary)" stroke-width="2" opacity="0.35">
            <animate attributeName="d" values="M140 180 Q135 170 140 160 Q145 150 140 140;M140 175 Q145 165 140 155 Q135 145 140 135;M140 180 Q135 170 140 160 Q145 150 140 140" dur="2.8s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="0.35;0.05;0.35" dur="2.8s" repeatCount="indefinite" />
          </path>
        </svg>
      </div>

      <!-- 500 数字 -->
      <h1 class="error-code">
        <span class="digit">5</span>
        <span class="digit fridge-o">
          <svg viewBox="0 0 60 80" class="mini-fridge">
            <rect x="8" y="4" width="44" height="72" rx="6" fill="none" stroke="currentColor" stroke-width="4" />
            <line x1="8" y1="30" x2="52" y2="30" stroke="currentColor" stroke-width="3" />
            <rect x="42" y="14" width="4" height="12" rx="2" fill="currentColor" />
            <rect x="42" y="40" width="4" height="12" rx="2" fill="currentColor" />
          </svg>
        </span>
        <span class="digit">0</span>
      </h1>

      <!-- 文案 -->
      <h2 class="error-title">冰箱内部发生了故障 — 我们的工程师正在抢修</h2>
      <p class="error-desc">
        服务器遇到了意外错误，请稍后再试，或联系技术支持团队
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

/* 火花背景 */
.sparks {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.spark {
  position: absolute;
  width: 4px;
  height: 4px;
  background: var(--danger-color);
  border-radius: 50%;
  opacity: 0;
  animation: spark-fly ease-out infinite;
}

.spark:nth-child(1) { left: 20%; top: 60%; animation-duration: 2s; animation-delay: 0s; }
.spark:nth-child(2) { left: 35%; top: 55%; animation-duration: 2.5s; animation-delay: 0.5s; width: 6px; height: 6px; }
.spark:nth-child(3) { left: 50%; top: 65%; animation-duration: 1.8s; animation-delay: 1s; }
.spark:nth-child(4) { left: 65%; top: 50%; animation-duration: 2.2s; animation-delay: 1.5s; width: 5px; height: 5px; }
.spark:nth-child(5) { left: 80%; top: 60%; animation-duration: 2.8s; animation-delay: 0.8s; }
.spark:nth-child(6) { left: 15%; top: 45%; animation-duration: 2s; animation-delay: 1.2s; width: 5px; height: 5px; }
.spark:nth-child(7) { left: 45%; top: 40%; animation-duration: 2.4s; animation-delay: 0.3s; }
.spark:nth-child(8) { left: 75%; top: 45%; animation-duration: 2.1s; animation-delay: 0.9s; width: 6px; height: 6px; }
.spark:nth-child(9) { left: 30%; top: 70%; animation-duration: 2.6s; animation-delay: 1.8s; }
.spark:nth-child(10) { left: 55%; top: 75%; animation-duration: 1.9s; animation-delay: 1.1s; width: 5px; height: 5px; }
.spark:nth-child(11) { left: 85%; top: 70%; animation-duration: 2.3s; animation-delay: 0.6s; }
.spark:nth-child(12) { left: 10%; top: 55%; animation-duration: 2.7s; animation-delay: 1.4s; width: 5px; height: 5px; }

@keyframes spark-fly {
  0% {
    transform: translate(0, 0) scale(1);
    opacity: 0;
  }
  20% {
    opacity: 0.8;
  }
  80% {
    opacity: 0.4;
  }
  100% {
    transform: translate(var(--tx, 30px), var(--ty, -60px)) scale(0);
    opacity: 0;
  }
}

/* 给不同的火花不同的飞行方向 */
.spark:nth-child(1) { --tx: 20px; --ty: -50px; }
.spark:nth-child(2) { --tx: -30px; --ty: -70px; }
.spark:nth-child(3) { --tx: 40px; --ty: -40px; }
.spark:nth-child(4) { --tx: -20px; --ty: -60px; }
.spark:nth-child(5) { --tx: 30px; --ty: -80px; }
.spark:nth-child(6) { --tx: -40px; --ty: -50px; }
.spark:nth-child(7) { --tx: 25px; --ty: -65px; }
.spark:nth-child(8) { --tx: -35px; --ty: -45px; }
.spark:nth-child(9) { --tx: 15px; --ty: -55px; }
.spark:nth-child(10) { --tx: -25px; --ty: -75px; }
.spark:nth-child(11) { --tx: 35px; --ty: -50px; }
.spark:nth-child(12) { --tx: -15px; --ty: -70px; }

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
  filter: drop-shadow(0 8px 24px rgba(248, 113, 113, 0.2));
}

/* 500 数字 */
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
  background: linear-gradient(135deg, var(--danger-color), var(--danger-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.fridge-o {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--danger-color);
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
  background: linear-gradient(90deg, var(--danger-color), var(--danger-dark));
  border: none;
  font-weight: 500;
  padding: 0 var(--space-8);
  height: 44px;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(248, 113, 113, 0.3);
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
