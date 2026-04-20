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
        <!-- 这里将填充具体内容 -->
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
  background: var(--page-bg);
}

.main-content-wrapper {
  display: flex;
  flex: 1;
  margin-top: 64px; /* 与头部高度一致 */
}

.main-content {
  flex: 1;
  margin-left: 240px; /* 与侧边栏宽度一致 */
  background: var(--page-bg);
  min-height: calc(100vh - 64px - 80px); /* 视口高度减去头部和底部高度 */
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 200px; /* 与侧边栏宽度一致 */
    padding: 16px;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .main-content {
    margin-left: 0;
    padding: 12px;
  }
}
</style>