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
        <EnhancedInput
            v-model="loginForm.account"
            placeholder="请输入用户名或手机号"
            icon="icon-contact"
        />
      </el-form-item>

      <el-form-item prop="password">
        <EnhancedInput
            v-model="loginForm.password"
            placeholder="请输入密码"
            icon="icon-lock"
            type="password"
            :show-password="true"
        />
      </el-form-item>

      <el-form-item prop="captcha">
        <div class="captcha-container">
          <EnhancedInput
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              icon="icon-captcha"
              style="width: 60%"
          />
          <div class="captcha-image-container">
            <img
                :src="captchaUrl"
                alt="验证码"
                class="captcha-image"
                @click="refreshCaptcha"
                title="点击刷新验证码"
            />
          </div>
        </div>
      </el-form-item>

      <el-form-item class="remember-me-item">
        <div class="remember-me-wrapper">
          <el-checkbox v-model="loginForm.rememberMe" class="custom-checkbox">
            下次自动登录
          </el-checkbox>
          <el-tooltip
              content="勾选后30天内无需输入密码自动登录"
              placement="right"
              effect="light"
              :show-after="200"
          >
            <i class="tips-icon iconfont icon-info-box" />
          </el-tooltip>
        </div>
      </el-form-item>

      <AuthButtonGroup
          primary-text="登 录"
          secondary-text="注 册"
          :loading="loading"
          loading-text="登录中..."
          @primary-action="handleLogin"
          @secondary-action="handleRegister"
      />
    </el-form>
  </AuthLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import showMessage from '@/utils/message'
import axios from 'axios'
import AuthLayout from "@/components/auth/AuthLayout.vue"
import AuthButtonGroup from "@/components/auth/AuthButtonGroup.vue"
import EnhancedInput from "@/components/auth/EnhancedInput.vue";

const router = useRouter()
const userStore = useUserStore()
const systemStore = useSystemStore()
const { systemName, getSystemInfo } = systemStore
const loginFormRef = ref()
const loading = ref(false)

// 初始化系统信息
onMounted(async () => {
  await getSystemInfo()
  // 初始化验证码
  await refreshCaptcha()
})

// 验证码图片URL
const captchaUrl = ref('/captcha/generate')
// 验证码ID
const captchaId = ref('')

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
    { required: true, message: '用户名或手机号不能为空', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '验证码不能为空', trigger: 'blur' },
    { min: 4, max: 4, message: '验证码长度为4位', trigger: 'blur' }
  ]
}

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    // 使用axios直接请求验证码，获取响应头中的X-Captcha-Id
    const response = await axios({
      url: `/captcha/generate?timestamp=${Date.now()}`,
      method: 'get',
      responseType: 'blob',
      baseURL: import.meta.env.VITE_API_BASE_URL
    })

    // 从响应头中获取captchaId
    captchaId.value = response.headers['x-captcha-id'] || ''
    
    // 创建图片URL
    captchaUrl.value = URL.createObjectURL(response.data)
  } catch (error) {
    console.error('刷新验证码失败:', error)
  }
}

// 登录方法
const handleLogin = async () => {
  if (loading.value) return

  try {
    await loginFormRef.value.validate()

    loading.value = true

    // 传递captchaId到登录请求
    const loginData = {
      ...loginForm.value,
      captchaId: captchaId.value
    }
    const res = await userStore.login(loginData)

    // 校验后端返回的code是否为200
    if (res.code === 200) {
      const message = res.data?.rememberMe
          ? '登录成功！30天内自动登录'
          : (res.message || '登录成功！')
      showMessage.success(message)

      await router.push('/user/index')
    } else {
      showMessage.error(res.message || '登录失败')
      // 登录失败时刷新验证码
      await refreshCaptcha()
    }
  } catch (error) {
    if (error?.message) {
      console.log('表单验证失败')
    } else {
      // 网络错误等异常情况
      console.error('登录失败:', error)
      // 异常时刷新验证码
      refreshCaptcha()
    }
  } finally {
    loading.value = false
  }
}

// 注册按钮（功能留空）
const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
@import '@/assets/theme.css';

/* 认证表单 */
.auth-form {
    margin-top: 10px;
}

.auth-form :deep(.el-form-item) {
    margin-bottom: 20px;
}

/* 记住我选项 */
.remember-me-item {
    margin-bottom: 24px !important;
}

.remember-me-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
}

/* 提示图标 */
.tips-icon {
    color: var(--primary-color);
    font-size: 20px !important;
    cursor: help;
    transition: all 0.3s ease;
}

.tips-icon:hover {
    color: var(--primary-dark);
    transform: scale(1.1);
}

/* 验证码容器 */
.captcha-container {
    display: flex;
    align-items: center;
    gap: 12px;
}

.captcha-image-container {
    flex: 1;
    display: flex;
    justify-content: flex-end;
}

.captcha-image {
    width: 130px;
    height: 50px;
    cursor: pointer;
    border-radius: 12px;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px var(--primary-10);
}

.captcha-image:hover {
    opacity: 0.9;
    transform: scale(1.02);
    box-shadow: 0 4px 12px var(--primary-20);
}
</style>