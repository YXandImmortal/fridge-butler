<template>
  <footer class="copyright-footer">
    <div class="copyright-content">
      <span class="copyright-text">
        © 2026 智鲜·引擎
      </span>
      <span class="divider">|</span>
      <span class="copyright-text">by
      <a href="https://github.com/YXandImmortal" target="_blank" rel="noopener noreferrer" class="font-link">YXandImmortal</a>
      </span>
      <span class="divider">|</span>
      <span class="font-info">
        界面使用
        <a href="https://hyperos.mi.com/font" target="_blank" rel="noopener noreferrer" class="font-link">
          MiSans
        </a>
        开源字体
      </span>
    </div>
  </footer>
</template>

<script setup>
// 无需额外逻辑
</script>

<style scoped>
.copyright-footer {
  width: 100%;
  padding: 20px 24px;
  background: radial-gradient(ellipse, rgba(255, 255, 255, 1) 0%, rgba(248, 250, 252, 0.85) 100%);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(100, 181, 246, 0.15);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.03);
  position: relative;
  box-sizing: border-box;
}

.copyright-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  max-width: 1200px;
  margin: 0 auto;
}

.copyright-text {
  font-size: 14px;
  color: #4a5568;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.divider {
  color: #cbd5e0;
  font-size: 14px;
  user-select: none;
}

.font-info {
  font-size: 14px;
  color: #718096;
}

.font-link {
  color: #64B5F6;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  padding-bottom: 2px;
}

.font-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #64B5F6, #81D4FA);
  transition: width 0.3s ease;
  border-radius: 1px;
}

.font-link:hover {
  color: #42A5F5;
}

.font-link:hover::after {
  width: 100%;
}

@media (max-width: 768px) {
  .copyright-footer {
    padding: 16px 20px;
  }

  .copyright-content {
    gap: 8px;
  }

  .copyright-text,
  .font-info {
    font-size: 13px;
  }

  .divider {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .copyright-content {
    flex-direction: column;
    gap: 6px;
  }

  .divider {
    display: none;
  }
}
</style>
