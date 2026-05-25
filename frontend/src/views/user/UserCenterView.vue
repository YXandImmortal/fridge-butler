<template>
  <div class="profile-container">
    <div class="profile-card">
      <h2 class="profile-title">个人中心</h2>
      <div class="profile-avatar">
        <div class="avatar-wrapper" @click="handleChangeAvatar">
          <Avatar size="x-large" :avatar-id="userForm.avatar"/>
          <div class="avatar-edit-icon">
            <i class="iconfont icon-edit-box" />
          </div>
        </div>
      </div>
      <el-form :model="userForm" label-position="top" class="profile-form">
        <el-form-item label="用户名">
          <EnhancedInput v-model="userForm.username" placeholder="请输入用户名" icon="icon-contact" />
        </el-form-item>

        <el-form-item label="手机号">
          <EnhancedInput v-model="userForm.mobile" placeholder="请输入手机号" icon="icon-device-phone" />
        </el-form-item>

        <el-form-item label="注册时间">
          <EnhancedInput v-model="userForm.createTime" disabled icon="icon-calendar" />
        </el-form-item>

        <el-form-item label="角色">
          <EnhancedInput v-model="userForm.roleName" disabled icon="icon-user" />
        </el-form-item>
      </el-form>

      <div class="profile-actions">
        <CustomButton type="primary" @click="showConfirmSave = true" :loading="loadingSave" loading-text="保存中...">
          <i class="iconfont icon-save" />
          保存修改
        </CustomButton>
        <CustomButton @click="handleChangePassword">
          <i class="iconfont icon-edit-box" />
          修改密码
        </CustomButton>
        <CustomButton type="danger" @click="handleLogout">
          <i class="iconfont icon-logout" />
          退出登录
        </CustomButton>
      </div>
    </div>

    <!-- 编辑信息区域 -->
    <Transition name="slide-left">
      <div class="edit-card" v-show="showEditCard">
        <div class="edit-content">
          <!-- 编辑内容将根据编辑类型动态显示 -->
          <Transition name="switch">
            <div v-if="editType === 'password'">
              <h2 class="edit-title">修改密码</h2>
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top" class="profile-form edit-form">
                <el-form-item label="原密码" prop="originalPassword">
                  <EnhancedInput type="password" v-model="passwordForm.originalPassword" placeholder="请输入原密码" icon="icon-lock" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <EnhancedInput type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" icon="icon-lock" />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmNewPassword">
                  <EnhancedInput type="password" v-model="passwordForm.confirmNewPassword" placeholder="请确认新密码" icon="icon-lock" />
                </el-form-item>
                <el-form-item label="验证码" prop="captcha">
                  <CaptchaInput
                      v-model="passwordForm.captcha"
                      ref="captchaInputRef"
                      :height="40"
                      input-width="150px"
                  />
                </el-form-item>
              </el-form>
            </div>
            <div v-else-if="editType === 'avatar'">
              <h2 class="edit-title">选择头像</h2>
              <div class="avatar-upload-section">
                <Avatar size="x-large" :avatar-id="selectedAvatar"/>
                <div class="avatar-grid">
                  <div
                    v-for="avatarId in systemAvatars"
                    :key="avatarId"
                    class="avatar-item"
                    :class="{ 'selected': selectedAvatar === avatarId }"
                    @click="handleSelectAvatar(avatarId)"
                  >
                    <Avatar size="large" :avatar-id="avatarId"/>
                  </div>
                </div>
              </div>
            </div>
          </Transition>
        </div>
        <div class="edit-actions">
          <CustomButton
            type="primary"
            @click="editType === 'password' ? handleChangePasswordSubmit() : handleChangeAvatarSubmit()"
            :loading="editType === 'password' ? loadingChangePassword : loadingChangeAvatar"
            loading-text="修改中..."
          >
            <i class="iconfont icon-check" />
            确认
          </CustomButton>
          <CustomButton @click="showEditCard = false">
            <i class="iconfont icon-close" />
            取消
          </CustomButton>
        </div>
      </div>
    </Transition>

    <!-- 确认保存修改对话框 -->
    <ConfirmDialog
        v-model:visible="showConfirmSave"
        title="确定保存"
        message="您确定要保存吗？保存成功后需要重新登录"
        confirm-text="确定"
        cancel-text="取消"
        @confirm="handleSave"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import showMessage from '@/utils/message'
import { useUserStore } from '@/stores/user';
import Avatar from "@/components/Avatar.vue";
import EnhancedInput from "@/components/EnhancedInput.vue";
import CaptchaInput from "@/components/CaptchaInput.vue";
import { getSystemAvatarIds } from '@/utils/avatarManager';
import CustomButton from "@/components/CustomButton.vue";

const router = useRouter()
const userStore = useUserStore();
const { getUserInfo, updateUserInfo, changePassword, updateUserAvatar, logout } = userStore;

const showConfirmSave = ref(false);

// 控制编辑卡片显示/隐藏
const showEditCard = ref(false);

// 编辑类型：password 或 avatar
const editType = ref('');

// 头像相关
const systemAvatars = ref([]);
const selectedAvatar = ref('');
const loadingChangeAvatar = ref(false);

// 加载状态
const loadingSave = ref(false);
const loadingChangePassword = ref(false);

const captchaInputRef = ref();
const passwordFormRef = ref();

// 密码修改表单
const passwordForm = ref({
  originalPassword: '',
  newPassword: '',
  confirmNewPassword: '',
  captcha: ''
});

// 密码修改表单验证规则
const passwordRules = {
  originalPassword: [
    { required: true, message: '原密码不能为空', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 6, message: '新密码长度至少为6位', trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: true, message: '确认新密码不能为空', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      }, trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '验证码不能为空', trigger: 'blur' },
    { min: 4, max: 4, message: '验证码长度为4位', trigger: 'blur' }
  ]
};

// 用户信息表单
const userForm = ref({
  username: '',
  mobile: '',
  createTime: '',
  roleName: '',
  avatar: ''
});

// 初始化用户信息
onMounted(async () => {
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
  
  // 加载系统预设头像
  loadSystemAvatars();
});

// 加载系统预设头像
const loadSystemAvatars = () => {
  systemAvatars.value = getSystemAvatarIds();
};


// 保存修改
const handleSave = async () => {
  if (loadingSave.value) return;

  try {
    loadingSave.value = true;
    const res = await updateUserInfo(userForm.value);

    if (res.code === 200) {
      showMessage.success('保存成功');
      handleLogout('保存成功，请重新登录')
    } else {
      showMessage.error('保存失败: ' + (res.message || '未知错误'));
    }
  } catch (error) {
    showMessage.error('保存失败');
    console.error('保存失败:', error);
  } finally {
    loadingSave.value = false;
  }
};

// 修改密码
const handleChangePassword = async () => {
  editType.value = 'password';
  showEditCard.value = true;
  // 初始化验证码
  await captchaInputRef.value?.refreshCaptcha();
};

// 提交修改密码
const handleChangePasswordSubmit = async () => {
  if (loadingChangePassword.value) return;

  try {
    await passwordFormRef.value.validate();

    loadingChangePassword.value = true;

    // 传递captchaId到修改密码请求
    const changePasswordData = {
      ...passwordForm.value,
      captchaId: captchaInputRef.value?.captchaId || ''
    };

    console.log(changePasswordData);

    // 调用修改密码接口
    const res = await changePassword(changePasswordData);

    if (res.code === 200) {
      showMessage.success('密码修改成功');
      showEditCard.value = false;
      // 重置表单
      passwordForm.value = {
        originalPassword: '',
        newPassword: '',
        confirmNewPassword: '',
        captcha: ''
      };
    } else {
      showMessage.error(res.message || '密码修改失败');
      // 失败时刷新验证码
      await captchaInputRef.value?.refreshCaptcha();
    }
  } catch (error) {
    if (error?.message) {
      console.log('表单验证失败');
    } else {
      // 网络错误等异常情况
      console.error('修改密码失败:', error);
      showMessage.error('修改密码失败');
      // 异常时刷新验证码
      await captchaInputRef.value?.refreshCaptcha();
    }
  } finally {
    loadingChangePassword.value = false;
  }
};

// 修改头像
const handleChangeAvatar = () => {
  editType.value = 'avatar';
  selectedAvatar.value = userForm.value.avatar;
  showEditCard.value = true;
};

// 选择头像
const handleSelectAvatar = (avatarId) => {
  selectedAvatar.value = avatarId;
};

// 提交修改头像
const handleChangeAvatarSubmit = async () => {
  if (loadingChangeAvatar.value) return;
  try {
    loadingChangeAvatar.value = true;
    const res = await updateUserAvatar(selectedAvatar.value);
    
    if (res.code === 200) {
      showMessage.success('头像修改成功');
      userForm.value.avatar = selectedAvatar.value;
      showEditCard.value = false;
    } else {
      showMessage.error('头像修改失败: ' + (res.message || '未知错误'));
    }
  } catch (error) {
    showMessage.error('头像修改失败');
    console.error('头像修改失败:', error);
  } finally {
    loadingChangeAvatar.value = false;
  }
};

// 处理退出登录
const handleLogout = (msg) => {
  logout();
  showConfirmSave.value = false;
  router.push('/login');
  showMessage.info(msg || '已退出登录')
};
</script>

<style scoped lang="scss">
.profile-container {
  width: 100%;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  min-height: 100%;
  gap: 32px;
  animation: fade-in-up 0.6s ease-out;
  flex-wrap: wrap;
}

.profile-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 12px;
  text-align: center;
  color: var(--text-primary);
  animation: fade-in-down 0.6s ease-out;
}

.edit-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 12px;
  text-align: center;
  color: var(--text-primary);
}

.profile-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
  animation: fadeIn 0.8s ease-out;
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

.profile-card {
  max-width: 600px;
  width: 100%;
  padding: 40px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  z-index: 2;
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
  margin-bottom: 24px;
}

.profile-form.edit-form .el-form-item {
  width: 100%;
}

.profile-form .el-form-item__label {
  text-align: center;
  width: 100%;
  margin-bottom: 12px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 16px;
}

.profile-actions {
  display: flex;
  gap: var(--space-5);
  justify-content: center;
  padding-top: 24px;
  border-top: 1px solid var(--divider-color);
}

.edit-content {
  position: relative;
  width: 100%;
}

/* 编辑卡片样式 */
.edit-card {
  max-width: 600px;
  padding: 40px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  z-index: 1;
}

.edit-card:hover {
  box-shadow: 0 12px 60px var(--glass-lavender-25);
  transform: translateY(-2px);
}

.avatar-upload-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  width: 100%;
  max-width: 300px;
}

.avatar-item {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-item:hover {
  background-color: var(--primary-light);
  transform: scale(1.05);
}

.avatar-item.selected {
  background-color: var(--primary-20);
  border: 2px solid var(--primary-color);
}

.avatar-item :deep(.el-avatar) {
  transition: all 0.3s ease;
}

.avatar-item:hover :deep(.el-avatar) {
  box-shadow: var(--shadow-avatar);
  transition: all 0.3s ease;
}

.edit-actions {
  display: flex;
  justify-content: center;
  gap: var(--space-5);
  padding-top: 24px;
  border-top: 1px solid var(--divider-color);
}

/* 响应式设计 */
@media (max-width: 1280px) {
  .profile-container {
    flex-direction: column;
    align-items: center;
  }

  .edit-card {
    animation: fade-in-up 0.6s ease-out;
  }
}

@media (max-width: 768px) {
  .profile-card,
  .edit-card {
    padding: 32px 24px;
    max-width: 90%;
  }

  .profile-title {
    font-size: 28px;
    margin-bottom: 24px;
  }

  .profile-actions,
  .edit-actions {
    flex-direction: column;
    gap: 12px;
  }

  .profile-actions .custom-button,
  .edit-actions .custom-button {
    width: 100%;
    min-width: auto;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
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
