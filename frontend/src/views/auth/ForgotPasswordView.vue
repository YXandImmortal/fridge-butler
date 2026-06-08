<template>
  <AuthLayout
    :title="step === 1 ? '忘记密码' : '重置密码'"
    :subtitle="step === 1 ? '请输入您绑定的邮箱地址' : '请输入验证码和新密码'"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      class="auth-form"
      @keyup.enter="handlePrimaryAction"
    >
      <!-- 步骤1：输入邮箱 -->
      <template v-if="step === 1">
        <el-form-item prop="email">
          <EnhancedInput
            v-model="form.email"
            placeholder="请输入绑定的邮箱地址"
            icon="icon-mail"
            :clearable="true"
          />
        </el-form-item>

        <AuthButtonGroup
          primary-text="下一步"
          secondary-text="返回登录"
          :loading="loading"
          loading-text="处理中..."
          @primary-action="handleNextStep"
          @secondary-action="handleBackToLogin"
        />
      </template>

      <!-- 步骤2：输入验证码 + 新密码 -->
      <template v-else>
        <div class="email-hint">
          <span class="email-hint-text">
            验证码已发送至 <strong>{{ maskedEmail }}</strong>
          </span>
        </div>

        <el-form-item prop="captcha">
          <EnhancedInput
            v-model="form.captcha"
            placeholder="请输入6位邮箱验证码"
            icon="icon-captcha"
            maxlength="6"
          />
        </el-form-item>

        <el-form-item prop="newPassword">
          <EnhancedInput
            v-model="form.newPassword"
            placeholder="请输入新密码"
            icon="icon-lock"
            type="password"
            :show-password="true"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <EnhancedInput
            v-model="form.confirmPassword"
            placeholder="请再次输入新密码"
            icon="icon-lock"
            type="password"
            :show-password="true"
          />
        </el-form-item>

        <AuthButtonGroup
          primary-text="重置密码"
          secondary-text="上一步"
          :loading="loading"
          loading-text="重置中..."
          @primary-action="handleReset"
          @secondary-action="step = 1"
        />
      </template>
    </el-form>

    <!-- Footer -->
    <div class="forgot-footer">
      <div v-if="step === 1">
        <span
            v-if="adminEmail"
            class="footer-text footer-link"
            @click="copyAdminEmail"
        >
        没有绑定邮箱？请联系管理员获取帮助
      </span>
        <span v-else class="footer-text">没有绑定邮箱？请联系管理员获取帮助</span>
      </div>
      <CustomButton
        v-if="step === 2"
        type="link"
        :disabled="resendCountdown > 0"
        @click="handleResend"
      >
        {{ resendCountdown > 0 ? `${resendCountdown}秒后重新发送` : '未收到验证码？重新发送' }}
      </CustomButton>
    </div>
  </AuthLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AuthLayout from '@/layouts/AuthLayout.vue'
import AuthButtonGroup from '@/components/auth/AuthButtonGroup.vue'
import EnhancedInput from '@/components/ui/EnhancedInput.vue'

import CustomButton from '@/components/ui/CustomButton.vue'
import request from '@/utils/request.js'
import showMessage from '@/utils/message.js'
import { getPublicConfig } from '@/api/system.js'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const step = ref(1)
const adminEmail = ref('')
const resendCountdown = ref(0)
let resendTimer = null

const form = reactive({
  email: '',
  captcha: '',
  newPassword: '',
  confirmPassword: ''
})

// 邮箱脱敏
const maskedEmail = computed(() => {
  const email = form.email
  if (!email) return ''
  const [local, domain] = email.split('@')
  if (!local || !domain) return email
  return local[0] + '***@' + domain
})

// 密码验证
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 8 || value.length > 20) {
    callback(new Error('密码长度应为8-20位'))
  } else {
    let typeCount = 0
    if (/[A-Z]/.test(value)) typeCount++
    if (/[a-z]/.test(value)) typeCount++
    if (/[0-9]/.test(value)) typeCount++
    if (/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(value)) typeCount++

    if (typeCount < 2) {
      callback(new Error('密码需包含大写字母、小写字母、数字、特殊符号中的至少两种'))
    } else {
      callback()
    }
  }
}

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      message: '邮箱格式不正确',
      trigger: 'blur'
    }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(async () => {
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

onUnmounted(() => {
  if (resendTimer) {
    clearInterval(resendTimer)
    resendTimer = null
  }
})

const handlePrimaryAction = () => {
  if (step.value === 1) {
    handleNextStep()
  } else {
    handleReset()
  }
}

const handleNextStep = async () => {
  if (loading.value) return
  try {
    await formRef.value.validateField('email')
  } catch {
    return
  }

  try {
    loading.value = true
    const res = await request({
      url: '/auth/email/captcha',
      method: 'post',
      data: {
        email: form.email,
        type: 'RESET'
      }
    })

    if (res.code === 200) {
      showMessage.success(res.message || '验证码已发送，请查收邮件')
      startResendCountdown()
      step.value = 2
    } else {
      showMessage.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = async () => {
  if (loading.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  try {
    loading.value = true
    const res = await request({
      url: '/auth/reset-password',
      method: 'post',
      data: {
        email: form.email,
        captcha: form.captcha,
        newPassword: form.newPassword,
        confirmPassword: form.confirmPassword
      }
    })

    if (res.code === 200) {
      showMessage.success(res.message || '密码重置成功，请使用新密码登录')
      setTimeout(() => {
        router.replace({ name: 'login' })
      }, 1500)
    } else {
      showMessage.error(res.message || '密码重置失败')
    }
  } catch (error) {
    console.error('密码重置失败:', error)
  } finally {
    loading.value = false
  }
}

const handleBackToLogin = () => {
  router.replace({ name: 'login' })
}

const handleResend = async () => {
  if (resendCountdown.value > 0) return
  try {
    const res = await request({
      url: '/auth/email/captcha',
      method: 'post',
      data: {
        email: form.email,
        type: 'RESET'
      }
    })

    if (res.code === 200) {
      showMessage.success(res.message || '验证码已重新发送，请查收邮件')
      startResendCountdown()
    } else {
      showMessage.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    console.error('重新发送验证码失败:', error)
  }
}

const startResendCountdown = () => {
  resendCountdown.value = 60
  resendTimer = setInterval(() => {
    resendCountdown.value--
    if (resendCountdown.value <= 0) {
      clearInterval(resendTimer)
      resendTimer = null
    }
  }, 1000)
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
.email-hint {
  margin-bottom: var(--space-4);
  text-align: center;
}

.email-hint-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.email-hint-text strong {
  color: var(--text-primary);
}

.forgot-footer {
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

/* 验证错误时的 focus 样式 */
.el-form-item.is-error :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-danger) inset;
  border-color: var(--el-color-danger);
}
</style>
