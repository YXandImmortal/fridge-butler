<template>
  <div class="super-admin-container">
    <div class="placeholder-card">
      <div class="placeholder-icon">
        <i class="iconfont icon-settings"/>
      </div>
      <h1 class="placeholder-title">超级管理员后台</h1>
      <p class="placeholder-text">管理员功能正在开发中</p>
      <div class="placeholder-progress">
        <div class="progress-bar"/>
      </div>
    </div>
  </div>
</template>

<script setup>
// 超级管理员首页 - 占位页面
</script>

<style scoped lang="scss">
.super-admin-container {
  max-width: 960px;
  margin: 0 auto;
  animation: fade-in-up 0.6s ease-out;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

.placeholder-card {
  text-align: center;
  padding: 48px 40px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  width: 100%;
  max-width: 480px;
}

.placeholder-card:hover {
  box-shadow: 0 12px 60px var(--glass-lavender-25);
  transform: translateY(-2px);
}

.placeholder-icon {
  width: 80px;
  height: 80px;
  background: var(--primary-light);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.placeholder-icon .iconfont {
  font-size: 36px;
  color: var(--primary-color);
}

.placeholder-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.placeholder-text {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.placeholder-progress {
  width: 100%;
  height: 6px;
  background: var(--divider-color);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  width: 40%;
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--primary-light));
  border-radius: 3px;
  animation: progress-move 2s ease-in-out infinite;
}

@keyframes progress-move {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(250%);
  }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .placeholder-card {
    padding: 36px 24px;
    margin: 0 16px;
  }

  .placeholder-title {
    font-size: 20px;
  }
}
</style>
