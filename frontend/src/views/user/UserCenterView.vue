<template>
  <div class="user-center-container">
    <!-- 页面标题 -->
    <div class="page-header animate-card">
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">管理个人信息，查看成就与冰箱保鲜数据</p>
    </div>

    <!-- 顶部双栏：个人信息 + 成就总览 -->
    <div class="top-section">
      <!-- 左侧：个人信息卡 -->
      <div class="profile-card animate-card card-delay-1">
        <div class="profile-avatar">
          <div class="avatar-wrapper" @click="handleChangeAvatar">
            <Avatar size="x-large" :avatar-id="userForm.avatar"/>
            <div class="avatar-edit-icon">
              <i class="iconfont icon-edit-box"/>
            </div>
          </div>
        </div>
        <h2 class="profile-title">个人信息</h2>
        <el-form :model="userForm" label-position="top" class="profile-form">
          <el-form-item label="用户名">
            <CustomInput v-model="userForm.username" placeholder="请输入用户名" icon="icon-contact"/>
          </el-form-item>

          <el-form-item label="手机号">
            <CustomInput v-model="userForm.mobile" placeholder="请输入手机号" icon="icon-device-phone"/>
          </el-form-item>

          <el-form-item label="邮箱">
            <CustomInput v-model="userForm.email" disabled placeholder="暂无绑定邮箱" icon="icon-mail"/>
          </el-form-item>
        </el-form>

        <div class="profile-actions">
          <CustomButton @click="showChangePasswordDialog = true">
            <i class="iconfont icon-edit-box"/>
            修改密码
          </CustomButton>
          <CustomButton @click="handleChangeEmail">
            <i class="iconfont icon-mail"/>
            {{ userForm.email ? '修改邮箱' : '绑定邮箱' }}
          </CustomButton>
          <CustomButton type="primary" @click="showConfirmSave = true" :loading="loadingSave" loading-text="保存中...">
            <i class="iconfont icon-save"/>
            保存修改
          </CustomButton>
          <CustomButton type="danger" @click="showConfirmLogout = true">
            <i class="iconfont icon-logout"/>
            退出登录
          </CustomButton>
        </div>
      </div>

      <!-- 右侧：成就总览面板 -->
      <div class="animate-card card-delay-2 achievement-panel-wrapper">
        <AchievementPanel
            :overview="gamificationStore.overview"
            :settings="gamificationStore.settings"
            :loading="gamificationStore.isOverviewLoading"
            @settings-click="activeTab = 'settings'"
            @toggle-collapse="handleTogglePanel"
            @tab-change="activeTab = $event"
        />
      </div>
    </div>

    <!-- 下方成就详情 Tabs -->
    <div class="achievement-tabs-card animate-card card-delay-3">
      <el-tabs v-model="activeTab" class="achievement-tabs">
        <el-tab-pane label="徽章墙" name="badges" lazy>
          <BadgeSection/>
        </el-tab-pane>
        <el-tab-pane label="EXP 日志" name="exp-log" lazy>
          <ExpLogSection/>
        </el-tab-pane>
        <el-tab-pane label="月度报告" name="reports" lazy>
          <MonthlyReportSection/>
        </el-tab-pane>
        <el-tab-pane label="成就设置" name="settings" lazy>
          <AchievementSettings/>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 修改密码弹窗 -->
    <ChangePasswordDialog
        v-model:visible="showChangePasswordDialog"
        @success="handlePasswordChanged"
    />

    <!-- 绑定/修改邮箱弹窗 -->
    <BindEmailDialog
        v-model:visible="showBindEmailDialog"
        :current-email="userForm.email"
        @success="handleEmailChanged"
    />

    <!-- 选择头像弹窗 -->
    <ChangeAvatarDialog
        v-model:visible="showChangeAvatarDialog"
        :current-avatar="userForm.avatar"
        @success="handleAvatarChanged"
    />

    <!-- 确认保存修改对话框 -->
    <ConfirmDialog
        v-model:visible="showConfirmSave"
        title="确定保存"
        message="您确定要保存吗？保存成功后需要重新登录"
        confirm-text="确定"
        cancel-text="取消"
        @confirm="handleSave"
    />
    <!-- 确认退出登录对话框 -->
    <ConfirmDialog
        v-model:visible="showConfirmLogout"
        title="退出登录"
        message="您确定要退出登录吗？"
        confirm-text="确定"
        cancel-text="取消"
        @confirm="handleLogout"
    />
    <UserCenterTour ref="tourRef"/>
  </div>
</template>

<script setup>
import UserCenterTour from '@/components/tour/UserCenterTour.vue'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'
import {onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {useGamificationStore} from '@/stores/gamification'
import Avatar from '@/components/ui/Avatar.vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import ChangePasswordDialog from '@/components/ui/ChangePasswordDialog.vue'
import BindEmailDialog from '@/components/user/BindEmailDialog.vue'
import ChangeAvatarDialog from '@/components/user/ChangeAvatarDialog.vue'
import AchievementPanel from '@/components/gamification/AchievementPanel.vue'
import BadgeSection from '@/components/gamification/BadgeSection.vue'
import ExpLogSection from '@/components/gamification/ExpLogSection.vue'
import MonthlyReportSection from '@/components/gamification/MonthlyReportSection.vue'
import AchievementSettings from '@/components/gamification/AchievementSettings.vue'
import showMessage from '@/utils/message'
import {resetLevelUpNotify} from '@/utils/levelUpNotify'
import {consumePendingRewards} from '@/utils/gamificationNotify'
import {destroyGamificationToastContainer} from '@/utils/gamificationToastContainer'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const gamificationStore = useGamificationStore()
const {getUserInfo, updateUserInfo, logout} = userStore

const showConfirmSave = ref(false)
const showConfirmLogout = ref(false)
const showChangePasswordDialog = ref(false)
const showBindEmailDialog = ref(false)
const showChangeAvatarDialog = ref(false)
const activeTab = ref('badges')

const loadingSave = ref(false)

const userForm = ref({
  username: '',
  mobile: '',
  email: '',
  roleName: '',
  avatar: ''
})

// 初始化用户信息
onMounted(async () => {
  const userInfo = await getUserInfo()
  if (userInfo) {
    userForm.value = {
      username: userInfo.username || '',
      mobile: userInfo.mobile || '',
      email: userInfo.email || '',
      roleName: userInfo.roleName || '',
      avatar: userInfo.avatar || 'egg'
    }
  } else {
    showMessage.error('获取用户信息失败')
  }

  // 若从通知跳转过来，自动打开邮箱绑定面板
  if (route.query.edit === 'email') {
    handleChangeEmail()
  }

  // 加载成就数据
  await gamificationStore.fetchOverview()
  await gamificationStore.fetchSettings()

  // 消费可能残留的待展示奖励（Header 通常已经消费过，此处作为兜底）
  await consumePendingRewards()
})

// 保存修改
const handleSave = async () => {
  if (loadingSave.value) return

  try {
    loadingSave.value = true
    const res = await updateUserInfo(userForm.value)

    if (res.code === 200) {
      showMessage.success('保存成功')
      handleLogout('保存成功，请重新登录')
    } else {
      showMessage.error('保存失败: ' + (res.message || '未知错误'))
    }
  } catch (error) {
    showMessage.error('保存失败')
    console.error('保存失败:', error)
  } finally {
    loadingSave.value = false
  }
}

// 修改密码
const handlePasswordChanged = () => {
  showChangePasswordDialog.value = false
}

// 修改邮箱
const handleChangeEmail = () => {
  showBindEmailDialog.value = true
}

const handleEmailChanged = (email) => {
  userForm.value.email = email
}

// 修改头像
const handleChangeAvatar = () => {
  showChangeAvatarDialog.value = true
}

const handleAvatarChanged = (avatar) => {
  userForm.value.avatar = avatar
}

// 折叠/展开成就面板
const handleTogglePanel = async () => {
  const newValue = !gamificationStore.isPanelHidden
  try {
    await gamificationStore.updateSettings({panelHidden: newValue})
  } catch (error) {
    console.error('切换成就面板状态失败:', error)
  }
}

// 处理退出登录
const handleLogout = (msg) => {
  logout()
  gamificationStore.reset()
  resetLevelUpNotify()
  destroyGamificationToastContainer()
  showConfirmSave.value = false
  showConfirmLogout.value = false
  router.push('/login')
  showMessage.info(typeof msg === 'string' ? msg : '已退出登录')
}

// 页面引导
const tourRef = ref(null)
const tourStore = useTourStore()

watch(() => tourStore.pendingStartScene, (scene) => {
  if (scene === TOUR_SCENES.USER_CENTER) {
    tourRef.value?.start()
  }
})
</script>

<style scoped lang="scss">
.user-center-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.animate-card {
  animation: fade-in-up 0.6s ease-out;
  animation-fill-mode: both;
}

.card-delay-1 {
  animation-delay: 0.1s;
}

.card-delay-2 {
  animation-delay: 0.2s;
}

.card-delay-3 {
  animation-delay: 0.3s;
}

.achievement-panel-wrapper {
  min-width: 0;
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.top-section {
  display: grid;
  grid-template-columns: 380px minmax(0, 1fr);
  gap: var(--space-6);
  margin-bottom: var(--space-6);
  align-items: stretch;
}

.profile-card {
  padding: var(--space-6);
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: stretch;
  min-width: 0;

  &:hover {
    box-shadow: var(--shadow-lg);
  }
}

.profile-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: var(--space-5);
  text-align: center;
  color: var(--text-primary);
}

.profile-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-4);
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.avatar-wrapper :deep(.el-avatar) {
  box-shadow: var(--shadow-avatar);
  transition: all 0.3s ease;
}

.avatar-edit-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  background: var(--primary-color);
  color: var(--text-inverse);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  border: 2px solid var(--card-bg);
}

.avatar-edit-icon:hover {
  background: var(--primary-dark);
  transform: scale(1.1);
  box-shadow: var(--shadow-md);
}

.avatar-edit-icon .iconfont {
  font-size: 16px;
}

.profile-form {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.profile-form .el-form-item {
  margin-bottom: var(--space-4);
}

.profile-form .el-form-item__label {
  text-align: center;
  width: 100%;
  margin-bottom: 12px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.profile-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--divider-color);
}

.achievement-tabs-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  padding: var(--space-5) var(--space-6);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: var(--shadow-lg);
  }
}

.achievement-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
}

.achievement-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

.achievement-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
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

/* 响应式设计 */
@media (max-width: 1280px) {
  .top-section {
    grid-template-columns: 1fr;
  }

  .profile-card {
    max-width: 500px;
    margin: 0 auto;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .profile-card,
  .achievement-tabs-card {
    padding: var(--space-4) var(--space-5);
  }

  .profile-actions {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .profile-actions .custom-button {
    width: 100%;
    min-width: auto;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }

  .profile-card,
  .achievement-tabs-card {
    padding: var(--space-4);
  }

  .profile-form .el-form-item {
    margin-bottom: 16px;
  }
}
</style>
