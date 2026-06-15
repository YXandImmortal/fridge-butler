<template>
  <AuthLayout
      :title="systemName || '冰箱管理系统'"
      subtitle="欢迎回来，请登录您的账户"
  >
    <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="auth-form"
        @keyup.enter="handleLogin"
    >
      <el-form-item prop="account">
        <CustomInput
            v-model="loginForm.account"
            placeholder="请输入用户名或手机号"
            icon="icon-contact"
            size="large"
            autocomplete="username"
        />
      </el-form-item>

      <el-form-item prop="password">
        <CustomInput
            v-model="loginForm.password"
            placeholder="请输入密码"
            icon="icon-lock"
            type="password"
            :showPassword="true"
            size="large"
            autocomplete="current-password"
        />
      </el-form-item>

      <el-form-item prop="captcha">
        <CaptchaInput
            v-model="loginForm.captcha"
            ref="captchaInputRef"
        />
      </el-form-item>

      <el-form-item class="remember-me-item">
        <div class="remember-me-wrapper">
          <CustomCheckbox v-model="loginForm.rememberMe" label="下次自动登录"/>
          <el-tooltip
              content="勾选后30天内无需输入密码自动登录"
              placement="right"
              effect="light"
              :show-after="200"
          >
            <i class="tips-icon iconfont icon-info-box"/>
          </el-tooltip>
        </div>
      </el-form-item>

      <AuthButtonGroup
          primary-text="点击登录"
          secondary-text="没有账号？注册"
          :loading="loading"
          loading-text="登录中..."
          @primary-action="handleLogin"
          @secondary-action="handleRegister"
      />
    </el-form>

    <div class="login-footer">
      <span class="footer-text footer-link" @click="handleForgotPassword">
        忘记密码
      </span>
    </div>
  </AuthLayout>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user.js'
import {useSystemStore} from '@/stores/system.js'
import showMessage from '@/utils/message.js'
import AuthLayout from "@/layouts/AuthLayout.vue"
import AuthButtonGroup from "@/components/auth/AuthButtonGroup.vue"
import CustomInput from "@/components/ui/CustomInput.vue"
import CaptchaInput from "@/components/form/CaptchaInput.vue"
import CustomCheckbox from "@/components/ui/CustomCheckbox.vue"

const router = useRouter()
const userStore = useUserStore()
const systemStore = useSystemStore()
const {systemName, getSystemInfo} = systemStore
const loginFormRef = ref()
const captchaInputRef = ref()
const loading = ref(false)

// 初始化系统信息
onMounted(async () => {
  await getSystemInfo()
})

// 登录表单数据
const loginForm = ref({
  account: '',
  password: '',
  captcha: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  account: [
    {required: true, message: '用户名或手机号不能为空', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '密码不能为空', trigger: 'blur'}
  ],
  captcha: [
    {required: true, message: '验证码不能为空', trigger: 'blur'},
    {min: 4, max: 4, message: '验证码长度为4位', trigger: 'blur'}
  ]
}

// 登录方法
const handleLogin = async () => {
  if (loading.value) return

  // 先验证表单
  try {
    await loginFormRef.value.validate()
  } catch {
    return // 表单验证失败，直接返回
  }

  // 执行登录请求
  try {
    loading.value = true

    // 传递captchaId到登录请求
    const loginData = {
      ...loginForm.value,
      captchaId: captchaInputRef.value?.captchaId || ''
    }
    const res = await userStore.login(loginData)

    // 校验后端返回的code是否为200
    if (res.code === 200) {
      const message = res.data.rememberMe
          ? '登录成功！30天内自动登录'
          : (res.message || '登录成功！')
      showMessage.success(message)

      // 记录登录 EXP 与徽章待展示（跳转到首页后展示）
      const loginExp = res.data?.expGained ?? 0
      const loginBadges = res.data?.badgesUnlocked || []
      if (loginExp > 0 || loginBadges.length > 0) {
        sessionStorage.setItem('pending_login_exp', JSON.stringify({
          exp: loginExp,
          description: '每日登录',
          badges: loginBadges
        }))
      }

      // 记录登录后的等级信息，首页挂载后检测是否触发升级
      const loginLevel = res.data?.level
      if (loginLevel) {
        sessionStorage.setItem('pending_login_level', JSON.stringify({
          leveledUp: res.data?.leveledUp === true,
          level: loginLevel
        }))
      }

      // 根据角色跳转到对应首页
      if (userStore.roleId === 1) {
        await router.push({name: 'super-admin-dashboard'})
      } else {
        await router.push({name: 'user-index'})
      }
    } else {
      showMessage.error(res.message || '登录失败')
      // 登录失败时刷新验证码
      await captchaInputRef.value?.refreshCaptcha()
    }
  } catch (error) {
    // 网络错误、请求超时、拦截器reject等异常情况
    console.error('登录失败:', error)
    // 异常时刷新验证码
    await captchaInputRef.value?.refreshCaptcha()
  } finally {
    loading.value = false
  }
}

// 注册按钮（功能留空）
const handleRegister = () => {
  router.push('/register')
}

// 忘记密码
const handleForgotPassword = () => {
  router.push('/forgot-password')
}
</script>

<style scoped lang="scss">
/* 记住我选项 */
.remember-me-item {
  margin-bottom: var(--space-6);
}

.remember-me-item :deep(.el-form-item__content) {
  line-height: 1;
}

/* 提示图标 */
.tips-icon {
  color: var(--primary-color);
  font-size: var(--space-5);
  cursor: help;
  transition: all 0.3s ease;
}

.tips-icon:hover {
  color: var(--primary-dark);
  transform: scale(1.1);
}

/* 验证错误时的样式 */
.el-form-item.is-error :deep(.custom-input) {
  border-color: var(--el-color-danger);
}

.el-form-item.is-error :deep(.custom-input.is-focused) {
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3), 0 0 0 3px rgba(245, 108, 108, 0.15);
}

/* 忘记密码入口 */
.login-footer {
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