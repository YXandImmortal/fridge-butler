<template>
  <aside class="app-sidebar">
    <!-- 导航菜单 -->
    <nav class="sidebar-nav">
      <ul class="nav-menu">
        <li class="nav-item active">
          <a href="#" class="nav-link">
            <div class="nav-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
              </svg>
            </div>
            <span class="nav-text">首页</span>
          </a>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link">
            <div class="nav-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
              </svg>
            </div>
            <span class="nav-text">功能1</span>
          </a>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link">
            <div class="nav-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
              </svg>
            </div>
            <span class="nav-text">功能2</span>
          </a>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link">
            <div class="nav-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
              </svg>
            </div>
            <span class="nav-text">功能3</span>
          </a>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link">
            <div class="nav-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
              </svg>
            </div>
            <span class="nav-text">功能4</span>
          </a>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link">
            <div class="nav-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
              </svg>
            </div>
            <span class="nav-text">功能5</span>
          </a>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
// 导航栏组件逻辑
</script>

<style scoped>
.app-sidebar {
  width: 240px;
  height: calc(100vh - 64px);
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 64px;
  left: 0;
  z-index: 999;
  transition: all 0.3s ease;
  overflow-y: auto;
}

.sidebar-nav {
  padding: 24px 0;
}

.nav-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-bottom: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 24px;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: 0 12px 12px 0;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover {
  background: var(--primary-10);
  color: var(--primary-color);
  transform: translateX(4px);
}

.nav-item.active .nav-link {
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 600;
}

.nav-item.active .nav-link::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--primary-color);
  border-radius: 0 4px 4px 0;
}

.nav-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.nav-icon svg {
  width: 20px;
  height: 20px;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-sidebar {
    width: 200px;
  }

  .nav-link {
    padding: 10px 16px;
    gap: 12px;
  }

  .nav-text {
    font-size: 13px;
  }
}

/* 滚动条样式 */
.app-sidebar::-webkit-scrollbar {
  width: 6px;
}

.app-sidebar::-webkit-scrollbar-track {
  background: var(--primary-10);
}

.app-sidebar::-webkit-scrollbar-thumb {
  background: var(--primary-30);
  border-radius: 3px;
}

.app-sidebar::-webkit-scrollbar-thumb:hover {
  background: var(--primary-40);
}
</style>