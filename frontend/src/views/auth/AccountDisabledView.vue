<template>
  <AuthLayout
    title="账号已被禁用"
    subtitle="您的账户因异常原因已被系统禁用"
  >
    <div class="disabled-content" style="margin-top: -30px;">
      <div class="disabled-icon-wrapper">
        <i class="iconfont icon-close-box disabled-icon"/>
      </div>
      <p class="disabled-desc">
        您的账号已被系统管理员禁用，无法继续使用。
      </p>
      <p class="disabled-hint">
        如有疑问，请联系管理员处理
      </p>

      <div v-if="adminEmail" class="admin-email-box">
        <span class="email-label">管理员邮箱：</span>
        <span class="email-value">{{ adminEmail }}</span>
        <CustomButton type="primary" size="small" @click="copyAdminEmail">
          <i class="iconfont icon-copy"/>
          复制
        </CustomButton>
      </div>
    </div>

    <CustomButton
      type="primary"
      size="large"
      class="back-login-btn"
      @click="handleBackToLogin"
    >
      <i class="iconfont icon-login"/>
      返回登录页
    </CustomButton>
  </AuthLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import AuthLayout from '@/layouts/AuthLayout.vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import { getPublicConfig } from '@/api/system.js'
import showMessage from '@/utils/message.js'

const router = useRouter()
const userStore = useUserStore()

const adminEmail = ref('')

onMounted(async () => {
  // 确保已登出，清除本地登录态
  if (userStore.isLoggedIn) {
    userStore.logout()
  }

  // 获取公开配置中的管理员邮箱
  try {
    const res = await getPublicConfig()
    if (res.code === 200 && res.data) {
      adminEmail.value = res.data.adminEmail || ''
    }
  } catch (error) {
    console.error('获取公开配置失败:', error)
  }
})

const handleBackToLogin = () => {
  router.replace({ name: 'login' })
}

const copyAdminEmail = async () => {
  if (!adminEmail.value) {
    showMessage.warning('暂无可用的管理员联系方式')
    return
  }
  try {
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(adminEmail.value)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = adminEmail.value
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    showMessage.success('管理员邮箱已复制')
  } catch {
    showMessage.error('复制失败，请手动复制')
  }
}
</script>

<style scoped lang="scss">
.disabled-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--space-4) 0;
}

.disabled-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--el-color-danger-light-9, rgba(245, 108, 108, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-5);
}

.disabled-icon {
  font-size: 40px;
  color: var(--el-color-danger);
}

.disabled-desc {
  font-size: 15px;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
  line-height: 1.6;
}

.disabled-hint {
  font-size: 13px;
  color: var(--text-tertiary);
  margin: 0 0 var(--space-5);
}

.admin-email-box {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  background: var(--primary-light);
  border: 1px dashed var(--primary-30);
  border-radius: var(--radius-sm);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-2);
  flex-wrap: wrap;
  justify-content: center;
}

.email-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.email-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
  font-family: 'Courier New', monospace;
}

.back-login-btn {
  width: 100%;
  margin-top: var(--space-4);
}

.back-login-btn :deep(.iconfont) {
  margin-right: var(--space-1);
}
</style>
