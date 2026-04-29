<template>
  <div class="index-container">
    <!-- 头部组件 -->
    <Header
      @show-logout-dialog="showLogoutDialog = true"
    />

    <!-- 主体内容区域 -->
    <div class="main-content-wrapper">
      <!-- 左侧导航栏 -->
      <Sidebar />

      <!-- 主内容区域 -->
      <main class="main-content">
        <router-view />
      </main>
    </div>

    <!-- 底部版权信息 -->
    <CopyrightFooter />
    
    <!-- 登出确认对话框 -->
    <ConfirmDialog
      v-model:visible="showLogoutDialog"
      title="退出登录"
      message="您确定要退出登录吗？"
      confirm-text="确定"
      cancel-text="取消"
      @confirm="handleLogout"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import Header from '@/components/Header.vue';
import Sidebar from '@/components/Sidebar.vue';
import CopyrightFooter from '@/components/CopyrightFooter.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import showMessage from '@/utils/message'
import { useSystemStore } from '@/stores/system';
import { useUserStore } from '@/stores/user';
import router from "@/router/index.js";

const systemStore = useSystemStore();
const userStore = useUserStore();
const { getSystemInfo } = systemStore;
const { logout } = userStore;

// 控制登出确认对话框显示/隐藏
const showLogoutDialog = ref(false);

// 初始化系统信息
onMounted(async () => {
  await getSystemInfo();
});

// 处理退出登录
const handleLogout = () => {
  logout();
  showLogoutDialog.value = false;
  router.push('/login');
  showMessage.info("已退出登录")
};
</script>

<style scoped>
.index-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content-wrapper {
  margin-top: var(--header-height);
}

.main-content {
  margin-left: var(--sidebar-width);
  transition: all 0.3s ease;
  max-height: calc(100vh - var(--header-height) - var(--footer-height));
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  background: var(--main-content-bg);
  padding: var(--space-5);
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: var(--sidebar-width-md);
    padding: var(--space-4);
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .main-content {
    margin-left: 0;
    padding: var(--space-3);
  }
}
</style>
