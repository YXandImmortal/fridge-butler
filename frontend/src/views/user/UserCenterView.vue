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
        <div class="profile-container">
          <div class="profile-card">
            <h2 class="profile-title">个人中心</h2>
            <div class="profile-avatar">
              <Avatar size="x-large" :avatar-id="userForm.avatar" />
            </div>
            <el-form :model="userForm" label-position="top" class="profile-form">
              <el-form-item label="用户名">
                <el-input v-model="userForm.username" placeholder="请输入用户名" />
              </el-form-item>

              <el-form-item label="手机号">
                <el-input v-model="userForm.mobile" placeholder="请输入手机号" />
              </el-form-item>

              <el-form-item label="注册时间">
                <el-input v-model="userForm.createTime" disabled />
              </el-form-item>

              <el-form-item label="角色">
                <el-input v-model="userForm.roleName" disabled />
              </el-form-item>
            </el-form>

            <div class="profile-actions">
              <el-button type="primary" @click="handleSave">保存修改</el-button>
              <el-button @click="handleChangePassword">修改密码</el-button>
              <el-button type="danger" @click="showLogoutDialog = true">退出登录</el-button>
            </div>
          </div>
        </div>
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
import Avatar from "@/components/Avatar.vue";

const systemStore = useSystemStore();
const userStore = useUserStore();
const { getSystemInfo } = systemStore;
const { logout, getUserInfo, updateUserInfo } = userStore;

// 控制登出确认对话框显示/隐藏
const showLogoutDialog = ref(false);

// 用户信息表单
const userForm = ref({
  username: '',
  mobile: '',
  createTime: '',
  roleName: '',
  avatar: ''
});

// 初始化系统信息和用户信息
onMounted(async () => {
  await getSystemInfo();
  const userInfo = await getUserInfo();
  if (userInfo) {
    userForm.value = {
      username: userInfo.username || '',
      mobile: userInfo.mobile || '',
      createTime: userInfo.createTime || '',
      roleName: userInfo.roleName || '',
      avatar: userInfo.avatar || 'bot'
    };
  } else {
    showMessage.error('获取用户信息失败');
  }
});



// 保存修改
const handleSave = async () => {
  try {
    const res = await updateUserInfo(userForm.value);

    if (res.code === 200) {
      showMessage.success('保存成功');
    } else {
      showMessage.error('保存失败: ' + (res.message || '未知错误'));
    }
  } catch (error) {
    showMessage.error('保存失败');
    console.error('保存失败:', error);
  }
};

// 修改密码（暂时留空）
const handleChangePassword = () => {
  // 暂时留空，后续实现
  showMessage.info('修改密码功能待实现');
};

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
  margin-top: 64px; /* 与头部高度一致 */
}

.main-content {
  margin-left: 240px; /* 与侧边栏宽度一致 */
  transition: all 0.3s ease;
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--main-content-bg);
  padding: 20px;
}

.profile-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeInUp 0.6s ease-out;
}

.profile-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 12px;
  text-align: center;
  color: var(--text-primary);
  animation: fadeInDown 0.6s ease-out;
}

.profile-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
  animation: fadeIn 0.8s ease-out;
}

.profile-avatar :deep(.el-avatar) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.profile-avatar :deep(.el-avatar:hover) {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.profile-card {
  max-width: 600px;
  width: 100%;
  padding: 40px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.profile-card:hover {
  box-shadow: 0 12px 60px var(--glass-lavender-25);
  transform: translateY(-2px);
}

.profile-form {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 表单项目样式优化 */
.profile-form .el-form-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.profile-form .el-form-item__label {
  text-align: center;
  width: 100%;
  margin-bottom: 12px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 16px;
}

/* 输入框增强样式 */
.profile-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 16px;
  box-shadow: 0 2px 8px var(--primary-10);
  transition: all 0.3s ease;
  border: 1px solid transparent;
  background: var(--card-bg);
}

.profile-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px var(--primary-20);
  border-color: var(--primary-light);
}

.profile-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px var(--primary-30);
  border-color: var(--primary-color);
}

.profile-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: var(--primary-light);
  opacity: 0.7;
}

.profile-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

/* 按钮增强样式 */
.profile-actions :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  min-width: 120px;
}

.profile-actions :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--primary-40);
}

.profile-actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  border: none;
}

.profile-actions :deep(.el-button--danger) {
  background: linear-gradient(135deg, var(--danger-color), #EF4444);
  border: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 200px;
    padding: 16px;
  }

  .profile-card {
    padding: 32px 24px;
    max-width: 90%;
  }

  .profile-title {
    font-size: 28px;
    margin-bottom: 24px;
  }

  .profile-actions {
    flex-direction: column;
    gap: 12px;
  }

  .profile-actions :deep(.el-button) {
    width: 100%;
    min-width: auto;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .main-content {
    margin-left: 0;
    padding: 12px;
  }

  .profile-card {
    padding: 24px 16px;
    max-width: 95%;
  }

  .profile-title {
    font-size: 24px;
    margin-bottom: 20px;
  }

  .profile-form .el-form-item {
    margin-bottom: 20px;
  }
}
</style>