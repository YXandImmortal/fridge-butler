<template>
  <AuthLayout
    title="系统内部测试阶段"
    subtitle="请输入激活密钥以继续使用"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      class="auth-form"
      @keyup.enter="handleSubmit"
    >
      <el-form-item prop="keyCode">
        <EnhancedInput
          v-model="form.keyCode"
          placeholder="请输入激活密钥，格式：FB-XXXXXXXX"
          icon="icon-key"
        />
      </el-form-item>

      <CustomButton
        type="primary"
        size="large"
        native-type="submit"
        :loading="loading"
        loading-text="激活中..."
        class="activate-btn"
        @click="handleSubmit"
      >
        激活账号
      </CustomButton>
    </el-form>

    <div class="activation-footer">
      <span
        v-if="adminEmail"
        class="footer-text footer-link"
        @click="copyAdminEmail"
      >
        没有密钥？请联系管理员
      </span>
      <span v-else class="footer-text">没有密钥？请联系管理员</span>
      <CustomButton type="link" @click="handleLogout">
        退出登录
      </CustomButton>
    </div>
  </AuthLayout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import AuthLayout from '@/layouts/AuthLayout.vue'
import EnhancedInput from '@/components/ui/EnhancedInput.vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import { verifyActivationKey } from '@/api/activation-key.js'
import { getPublicConfig } from '@/api/system.js'
import showMessage from '@/utils/message.js'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const adminEmail = ref('')
const form = ref({
  keyCode: ''
})

// 自动转大写
watch(() => form.value.keyCode, (val) => {
  if (val && val !== val.toUpperCase()) {
    form.value.keyCode = val.toUpperCase()
  }
})

const rules = {
  keyCode: [
    { required: true, message: '请输入激活密钥', trigger: 'blur' },
    { pattern: /^FB-[A-Z0-9]{8}$/, message: '密钥格式为 FB-XXXXXXXX', trigger: 'blur' }
  ]
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    router.replace({ name: 'login' })
    return
  }
  if (userStore.isActivated) {
    router.replace({ name: 'user-index' })
    return
  }
  if (userStore.roleId === 1) {
    router.replace({ name: 'super-admin-dashboard' })
    return
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

const handleSubmit = async () => {
  if (loading.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    const res = await verifyActivationKey({ keyCode: form.value.keyCode })
    if (res.code === 200 && res.data) {
      userStore.saveLoginData(res.data)
      showMessage.success('激活成功')
      router.replace({ name: 'user-index' })
    } else {
      showMessage.error(res.message || '激活失败')
    }
  } catch (error) {
    if (error?.message && !error.response) {
      // 表单验证失败或已处理的业务错误
      return
    }
    if (error.response) {
      // 响应拦截器已处理并提示，这里只打印日志
      console.error('激活失败:', error)
      return
    }
    console.error('激活失败:', error)
    showMessage.error('激活失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleLogout = () => {
  userStore.logout()
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
.activate-btn {
  width: 100%;
  margin-top: var(--space-4);
}

.activation-footer {
  margin-top: var(--space-6);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.footer-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.footer-link {
  cursor: pointer;
  text-decoration: underline;
  text-decoration-style: dashed;
  text-underline-offset: 3px;
  transition: color 0.2s ease;
}

.footer-link:hover {
  color: var(--primary-color);
}
</style>
