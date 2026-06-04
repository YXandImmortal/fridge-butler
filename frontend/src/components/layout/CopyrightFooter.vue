<template>
  <footer class="copyright-footer">
    <div class="copyright-content">
      <span class="copyright-text">
        © 2026 {{ systemName || '冰箱管理系统' }}
      </span>
      <span class="divider">|</span>
      <span class="copyright-text">by
      <a href="https://github.com/YXandImmortal" target="_blank" rel="noopener noreferrer" class="font-link">YXandImmortal</a>
      </span>
      <span class="divider">|</span>
      <span class="font-info">
        版本: {{ systemVersion || '1.0.0' }}
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
import {onMounted} from 'vue'
import {useSystemStore} from "@/stores/system.js"

const systemStore = useSystemStore()
const {systemName, systemVersion, getSystemInfo} = systemStore

// 初始化系统信息
onMounted(async () => {
  await getSystemInfo()
})
</script>

<style scoped lang="scss">
.copyright-footer {
  z-index: 1000;
  padding: 20px 24px;
  background: transparent;
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(100, 181, 246, 0.15);
  box-shadow: var(--shadow-footer);
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  box-sizing: border-box;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

/* 浅色背景层 */
.copyright-footer::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--gradient-footer-light);
  opacity: 1;
  transition: opacity 0.3s ease;
  z-index: -1;
  pointer-events: none;
}

/* 深色背景层 */
.copyright-footer::after {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--gradient-footer-dark);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
  pointer-events: none;
}

/* 深色模式下交换两层透明度 */
html.dark .copyright-footer::before {
  opacity: 0;
}

html.dark .copyright-footer::after {
  opacity: 1;
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
  color: var(--text-primary);
  font-weight: 500;
  letter-spacing: 0.3px;
}

.divider {
  color: var(--text-tertiary);
  font-size: 14px;
  user-select: none;
}

.font-info {
  font-size: 14px;
  color: var(--text-secondary);
}

.font-link {
  color: var(--primary-color);
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
  background: linear-gradient(90deg, var(--primary-color), var(--success-color));
  transition: width 0.3s ease;
  border-radius: 1px;
}

.font-link:hover {
  color: var(--primary-dark);
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