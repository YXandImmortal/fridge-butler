<template>
  <div class="user-index-container">
    <div class="welcome-card">
      <h1 class="welcome-title">欢迎使用冰箱管理系统</h1>
      <p class="welcome-subtitle">智能管理冰箱食材，让新鲜触手可及</p>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.user-index-container {
  max-width: 1200px;
  margin: 0 auto;
  animation: fade-in-up 0.6s ease-out;
}

.welcome-card {
  text-align: center;
  padding: 80px 40px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
  background: var(--gradient-text);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
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
  .welcome-card {
    padding: 60px 24px;
  }

  .welcome-title {
    font-size: 26px;
  }
}

@media (max-width: 480px) {
  .welcome-card {
    padding: 40px 20px;
  }

  .welcome-title {
    font-size: 22px;
  }
}
</style>
